package me.medisant.irongolem.service;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.medisant.irongolem.IronGolem;
import me.medisant.irongolem.config.ModConfig;
import me.medisant.irongolem.opencv.OpenCVUtils;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SkinChecker {

    private final IronGolem ironGolem;
    private final ExecutorService executorService;

    public SkinChecker(IronGolem ironGolem) {
        this.ironGolem = ironGolem;
        this.executorService = Executors.newSingleThreadExecutor();
    }


    public void checkSkinAsynchronous(File toCheck, GameProfile profile) {
        executorService.execute(() -> {
            List<CheckResult> checkResults = this.checkSkin(toCheck, profile);
            checkResults.forEach(checkResult -> {
                if (checkResult.matches()) ironGolem.onMatch(checkResult);
            });
        });
    }

    public void checkSkinAsynchronous(File toCheck, File model, GameProfile profile) {
        executorService.execute(() -> {
            CheckResult checkResult = this.checkSkin(toCheck, Imgcodecs.imread(model.getPath()), model.getPath(), profile);
            if (checkResult.matches()) ironGolem.onMatch(checkResult);
        });
    }

    private List<CheckResult> checkSkin(File toCheck, GameProfile profile) {
        List<CheckResult> results = Lists.newArrayList();
        for (Map.Entry<String, Mat> entry : ironGolem.getModels().entrySet()) {
            results.add(this.checkSkin(toCheck, entry.getValue(), entry.getKey(), profile));
        }
        return results;
    }

    private CheckResult checkSkin(File toCheck, Mat model, GameProfile profile) {
        return this.checkSkin(toCheck, model, "", profile);
    }

    private CheckResult checkSkin(File toCheck, Mat model, String modelPath, GameProfile profile) {
        Mat img = Imgcodecs.imread(toCheck.getPath());
        return new CheckResult(
                toCheck.getPath(),
                modelPath,
                OpenCVUtils.getPSNR(img, model),
                OpenCVUtils.getMSSIM(img, model),
                OpenCVUtils.compareHistograms(img, model),
                UUID.randomUUID(),
                profile.getName(),
                profile.getId().toString()
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CheckResult {
        public static CheckResult EMPTY = new CheckResult("", "", 100.0, 0.0, new double[]{0.0, 0.0, 0.0}, UUID.randomUUID(), "", "");
        private String toCheck;
        private String model;
        private double psnr;
        private double mssim;
        private double[] histogram;
        private UUID uuid;
        private String playerName;
        private String playerUUID;

        public boolean matches() {
            ModConfig config = ModConfig.instance;
            return config.psnr >= psnr
                    || config.mssim <= mssim;
                    //|| config.histogram_1 <= histogram[0]
                    //|| config.histogram_2 <= histogram[1]
                    //|| config.histogram_3 <= histogram[2];
        }

        public String toJson() {
            return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
        }
    }

}

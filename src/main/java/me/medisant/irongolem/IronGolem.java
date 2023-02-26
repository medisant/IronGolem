package me.medisant.irongolem;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.medisant.irongolem.config.ModConfig;
import me.medisant.irongolem.service.SkinChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class IronGolem implements ModInitializer {

    private static IronGolem instance;
    private final Map<String, Mat> models = Maps.newHashMap(); // path, model
    private final Map<UUID, Mat> checkedSkins = Maps.newHashMap(); // profile-UUID, skin
    private SkinChecker skinChecker;
    private File modelsDir;
    private File modDir;

    public static IronGolem getInstance() {
        //if (instance == null) instance = new IronGolem();
        return instance;
    }

    @Override
    public void onInitialize() {
        OpenCV.loadLocally();
        //OpenCV.loadShared();
        init();
    }

    public void init() {
        instance = this;
        checkedSkins.clear();
        skinChecker = new SkinChecker(this);
        initFileSystem();
        loadModels();
        ModConfig.init();
    }

    private void initFileSystem() {
        File gameDir = FabricLoader.getInstance().getGameDir().toFile();
        this.modDir = new File(gameDir, "irongolem");
        this.modelsDir = new File(modDir, "models");
        try {
            if (!modelsDir.exists()) Files.createDirectories(modelsDir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadModels() {
        for (File file : Objects.requireNonNull(this.modelsDir.listFiles())) {
            Mat mat = Imgcodecs.imread(file.getPath());
            this.models.putIfAbsent(file.getName(), mat);
        }
    }

    //temp
    public void onMatch(SkinChecker.CheckResult checkResult) {
        if (ModConfig.instance.printResult) {
            File result = new File(this.modDir.getPath() + File.separator + checkResult.getUuid() + "_" + checkResult.getModel().replaceFirst("\\.png", "") + ".json");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(result));
                writer.write(checkResult.toJson());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of(checkResult.getPlayerName() + " psnr: " + checkResult.getPsnr() + " mssim: " + checkResult.getMssim() + " histogram: " + Arrays.toString(checkResult.getHistogram())));
        }

    }

}

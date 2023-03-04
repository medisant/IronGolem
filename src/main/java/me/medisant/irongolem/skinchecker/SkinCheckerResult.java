package me.medisant.irongolem.skinchecker;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import me.medisant.irongolem.config.ModConfig;
import org.apache.commons.compress.utils.Lists;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SkinCheckerResult {

    private final File file;
    private final GameProfile profile;
    private final List<Check> checks;

    public SkinCheckerResult(File file, GameProfile profile) {
        this.file = file;
        this.profile = profile;
        this.checks = Lists.newArrayList();
    }

    public boolean hasPositiveMatch() {
        boolean flags = false;
        for (Check check: checks) {
            if (check.flags()) {
                flags = true;
                break;
            }
        }
        return flags;
    }

    public String toLogString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss").withZone(ZoneId.systemDefault());
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(formatter.format(Instant.now())).append("] ");
        if (profile.getName() != null) sb.append(profile.getName()).append(" ");
        if (profile.getId() != null) sb.append(profile.getId()).append(" ");
        sb.append(file.getPath()).append(" flags:");

        checks.forEach(check -> {
            if (check.flags()) {
                sb.append("\n                   ");
                sb.append("[").append(check.file.getName());
                sb.append(" psnr:").append(check.psnr);
                sb.append(" mssim:").append(check.mssim);
                sb.append(" histogram:").append(Arrays.toString(check.histogram));
                sb.append("]");
            }
        });
        return sb.toString();
    }

    public record Check(File file, double psnr, double mssim, double[] histogram) {

        public boolean flags() {
            ModConfig config = ModConfig.instance;
            return config.psnr >= psnr || config.mssim <= mssim || config.histogram_a <= histogram[0] || config.histogram_b <= histogram[1] || config.histogram_c <= histogram[2];
        }

    }
}

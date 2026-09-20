package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import player.Player;

public class CaptchaService {

    private static CaptchaService i;

    public static CaptchaService gI() {
        if (i == null) {
            i = new CaptchaService();
        }
        return i;
    }

    private CaptchaService() {
    }

    public void sendCaptcha(Player pl) throws IOException {
        File dir = new File("data/captcha");
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] list = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (list == null || list.length == 0) {
            return;
        }
        File file = list[new Random().nextInt(list.length)];
        String fileName = file.getName().replace(".png", "");
        String keyCaptcha = fileName;
        byte[] bytes = Files.readAllBytes(file.toPath());
        pl.idMark.setCaptcha(keyCaptcha);
        pl.idMark.setRecaptcha(System.currentTimeMillis());
        pl.captcha = "";
        String shuffledKey = shuffleString(keyCaptcha);
        Service.gI().sendCaptcha0(pl, shuffledKey, bytes);
    }

    public void receiveCaptcha(Player pl, char ch) throws IOException {
        if (pl == null) {
            return;
        }

        String expect = pl.idMark.getCaptcha();
        if (expect == null || expect.isEmpty()) {
            sendCaptcha(pl);
            return;
        }

        pl.captcha = (pl.captcha == null ? "" : pl.captcha) + ch;
        if (pl.captcha.contains(expect)) {
            Service.gI().sendCaptcha2(pl);
        } else if (pl.captcha.length() > 5) {
            pl.captcha = pl.captcha.substring(1);
            Service.gI().sendCaptcha1(pl);
        }
    }

    private String shuffleString(String input) {
        List<String> chars = Arrays.asList(input.split(""));
        Collections.shuffle(chars, new Random());
        StringBuilder sb = new StringBuilder();
        for (String c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
}

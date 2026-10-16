package com.mysteryvault.theme;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static BufferedImage referenceSheet = null;

    static {
        loadAssets();
    }

    private static void loadAssets() {
        // Load direct images
        referenceSheet = loadImageFromFileOrResource("reference_sheet.png");
        loadImageFromFileOrResource("menu_desk.jpg");
        loadImageFromFileOrResource("detective_portrait.jpg");
        loadImageFromFileOrResource("suspect_arjun.jpg");

        // Extract subimages from reference sheet if available
        if (referenceSheet != null) {
            extractReferenceSubimages();
        }
    }

    private static BufferedImage loadImageFromFileOrResource(String filename) {
        try {
            // First try file in src/main/resources/assets or assets/
            File f = new File("src/main/resources/assets/" + filename);
            if (!f.exists()) {
                f = new File("assets/" + filename);
            }
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    imageCache.put(filename, img);
                    return img;
                }
            }

            // Fallback to classpath
            InputStream is = AssetManager.class.getResourceAsStream("/assets/" + filename);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    imageCache.put(filename, img);
                    return img;
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load " + filename + ": " + e.getMessage());
        }
        return null;
    }

    private static void extractReferenceSubimages() {
        try {
            int rw = referenceSheet.getWidth();
            int rh = referenceSheet.getHeight();
            // Expected 1024x572
            float sx = rw / 1024.0f;
            float sy = rh / 572.0f;

            // Crop Meera (Suspect card center)
            cropAndCache("suspect_meera.png", (int)(116 * sx), (int)(230 * sy), (int)(48 * sx), (int)(48 * sy));
            // Crop Rohan (Suspect card right)
            cropAndCache("suspect_rohan.png", (int)(190 * sx), (int)(238 * sy), (int)(48 * sx), (int)(48 * sy));

            // Crop Case Selection Thumbnails (Screen 3)
            cropAndCache("case_diamond.png", (int)(534 * sx), (int)(62 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCache("case_library.png", (int)(630 * sx), (int)(62 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCache("case_poison.png", (int)(534 * sx), (int)(105 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCache("case_prototype.png", (int)(630 * sx), (int)(105 * sy), (int)(72 * sx), (int)(32 * sy));

            // Crop Evidence photo (Screen 6)
            cropAndCache("evidence_wineglass.png", (int)(360 * sx), (int)(232 * sy), (int)(122 * sx), (int)(56 * sy));

            // Crop Detective Desk background fragment for texture
            cropAndCache("desk_corkboard.png", 0, 0, (int)(240 * sx), (int)(180 * sy));
        } catch (Exception e) {
            System.err.println("Could not extract subimages: " + e.getMessage());
        }
    }

    private static void cropAndCache(String key, int x, int y, int w, int h) {
        if (referenceSheet == null) return;
        try {
            x = Math.max(0, Math.min(x, referenceSheet.getWidth() - 1));
            y = Math.max(0, Math.min(y, referenceSheet.getHeight() - 1));
            w = Math.max(1, Math.min(w, referenceSheet.getWidth() - x));
            h = Math.max(1, Math.min(h, referenceSheet.getHeight() - y));

            BufferedImage cropped = referenceSheet.getSubimage(x, y, w, h);
            // Upscale slightly for sharper rendering
            BufferedImage scaled = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(cropped, 0, 0, w * 2, h * 2, null);
            g2.dispose();

            imageCache.put(key, scaled);
        } catch (Exception ignored) {}
    }

    public static BufferedImage getImage(String name) {
        BufferedImage img = imageCache.get(name);
        if (img != null) return img;

        // Try loading if not cached yet
        img = loadImageFromFileOrResource(name);
        if (img != null) return img;

        // Procedural generation fallback
        return generateFallbackImage(name);
    }

    public static BufferedImage getCircularImage(BufferedImage source, int size, Color borderColor, int borderWidth) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        Theme.applyQualityHints(g2);

        Ellipse2D.Float circle = new Ellipse2D.Float(borderWidth, borderWidth, size - borderWidth * 2, size - borderWidth * 2);
        g2.setClip(circle);

        if (source != null) {
            g2.drawImage(source, borderWidth, borderWidth, size - borderWidth * 2, size - borderWidth * 2, null);
        } else {
            g2.setColor(Theme.CARD_BG_SOLID);
            g2.fill(circle);
        }

        g2.setClip(null);
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.draw(new Ellipse2D.Float(borderWidth / 2f, borderWidth / 2f, size - borderWidth, size - borderWidth));
        }

        g2.dispose();
        return output;
    }

    public static BufferedImage getRoundedImage(BufferedImage source, int width, int height, int arc, Color borderColor) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        Theme.applyQualityHints(g2);

        RoundRectangle2D round = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
        g2.setClip(round);

        if (source != null) {
            g2.drawImage(source, 0, 0, width, height, null);
        } else {
            g2.setColor(Theme.CARD_BG);
            g2.fill(round);
        }

        g2.setClip(null);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(round);
        }

        g2.dispose();
        return output;
    }

    private static BufferedImage generateFallbackImage(String name) {
        int w = 200, h = 150;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Theme.applyQualityHints(g2);

        // Dark moody background
        g2.setColor(Theme.CARD_BG);
        g2.fillRect(0, 0, w, h);

        g2.setColor(Theme.BORDER_CARD);
        g2.drawRect(0, 0, w - 1, h - 1);

        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.bold(12));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, (w - fm.stringWidth(name)) / 2, h / 2);

        g2.dispose();
        imageCache.put(name, img);
        return img;
    }
}

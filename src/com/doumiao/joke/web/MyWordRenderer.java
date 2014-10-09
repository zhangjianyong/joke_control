package com.doumiao.joke.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.google.code.kaptcha.text.WordRenderer;
import com.google.code.kaptcha.util.Configurable;

public class MyWordRenderer extends Configurable implements WordRenderer {
	public BufferedImage renderWord(String word, int width, int height) {
		int fontSize = getConfig().getTextProducerFontSize();
		Font[] fonts = getConfig().getTextProducerFonts(fontSize);
		Color color = getConfig().getTextProducerFontColor();
		int charSpace = 1;// getConfig().getTextProducerCharSpace();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = (Graphics2D) image.getGraphics();
		g2D.setColor(color);
		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY));
		g2D.setRenderingHints(hints);

		FontRenderContext frc = g2D.getFontRenderContext();
		Random random = new Random();
		// 随机产生的验证码
		char[] wordChars = word.toCharArray();
		Font[] chosenFonts = new Font[wordChars.length];
		int[] charWidths = new int[wordChars.length];
		int widthNeeded = 0;
		for (int i = 0; i < wordChars.length; ++i) {
			chosenFonts[i] = fonts[random.nextInt(fonts.length)];

			char[] charToDraw = { wordChars[i] };

			GlyphVector gv = chosenFonts[i].createGlyphVector(frc, charToDraw);
			charWidths[i] = (int) gv.getVisualBounds().getWidth();
			if (i > 0) {
				widthNeeded += 2;
			}
			widthNeeded += charWidths[i];
		}
		int startPosY = (height - fontSize) / 5 + fontSize;
		int startPosX = (width - widthNeeded) / 2;
		int pm[] = { -1, 1 };
		for (int i = 0; i < wordChars.length; i++) {
			g2D.setFont(chosenFonts[i]);
			int rad = random.nextInt(10) + 20;
			int pmv = pm[random.nextInt(2)];
			g2D.rotate(pmv * Math.toRadians(rad), startPosX, startPosY);
			char[] charToDraw = new char[] { wordChars[i] };
			g2D.drawChars(charToDraw, 0, charToDraw.length, startPosX,
					startPosY);
			g2D.rotate(-pmv * Math.toRadians(rad), startPosX, startPosY);
			startPosX = startPosX + (int) charWidths[i] + charSpace;
		}
		return image;
	}
}

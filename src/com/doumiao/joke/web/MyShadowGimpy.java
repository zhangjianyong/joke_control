package com.doumiao.joke.web;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.google.code.kaptcha.GimpyEngine;
import com.google.code.kaptcha.NoiseProducer;
import com.google.code.kaptcha.util.Configurable;
import com.jhlabs.image.RippleFilter;
import com.jhlabs.image.ShadowFilter;
import com.jhlabs.image.TransformFilter;

/**
 * {@link MyShadowGimpy} adds shadow to the text on the image and two noises.
 */
public class MyShadowGimpy extends Configurable implements GimpyEngine {
	/**
	 * Applies distortion by adding shadow to the text and also two noises.
	 * 
	 * @param baseImage
	 *            the base image
	 * @return the distorted image
	 */
	public BufferedImage getDistortedImage(BufferedImage baseImage) {
	   
		NoiseProducer noiseProducer = getConfig().getNoiseImpl();
		BufferedImage distortedImage = new BufferedImage(baseImage.getWidth(),
				baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graph = (Graphics2D) distortedImage.getGraphics();

		ShadowFilter shadowFilter = new ShadowFilter();
		shadowFilter.setRadius(8);
		shadowFilter.setDistance(5);
		shadowFilter.setOpacity(0);
		// shadowFilter.setAngle(1000);
		// shadowFilter.setShadowOnly(false);

		Random rand = new Random();

		RippleFilter rippleFilter = new RippleFilter();
		rippleFilter.setWaveType(RippleFilter.SINE);
		rippleFilter.setXAmplitude(4f);
		rippleFilter.setYAmplitude(rand.nextFloat() + 1.0f);
		rippleFilter.setXWavelength(rand.nextInt(10) + 8);
		rippleFilter.setYWavelength(rand.nextInt(5) + 2);
		rippleFilter.setEdgeAction(TransformFilter.RGB_CLAMP);
		BufferedImage effectImage = rippleFilter.filter(baseImage, null);
		effectImage = shadowFilter.filter(effectImage, null);

		graph.drawImage(effectImage, 0, 0, null, null);
		graph.dispose();

		// draw lines over the image and/or text
		noiseProducer.makeNoise(distortedImage, .1f, .1f, .25f, .25f);
		noiseProducer.makeNoise(distortedImage, .1f, .25f, .5f, .9f);

		return distortedImage;
	}
}

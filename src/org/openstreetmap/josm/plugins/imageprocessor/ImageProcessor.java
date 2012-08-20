package org.openstreetmap.josm.plugins.imageprocessor;


import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class ImageProcessor extends Plugin{

	public ImageProcessor(PluginInformation info) {
		super(info);
		GeoImageLayer.registerMenuAddition(new ImageProcessingAction());

	}
}

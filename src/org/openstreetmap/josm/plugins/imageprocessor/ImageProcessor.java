package org.openstreetmap.josm.plugins.imageprocessor;


import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
/**
 * ImageProcessor extends Plugin
 * @author nikhil
 *
 */
public class ImageProcessor extends Plugin{

	/**
	 * Add menu to geoimagelayer and invokes ImageProcessingAction()
	 * @param info PluginInformation of plugin
	 */
	public ImageProcessor(PluginInformation info) {
		super(info);
		GeoImageLayer.registerMenuAddition(new ImageProcessingAction());

	}
}

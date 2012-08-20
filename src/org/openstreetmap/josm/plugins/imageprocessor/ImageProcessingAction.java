package org.openstreetmap.josm.plugins.imageprocessor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;


class ImageProcessingAction extends AbstractAction implements LayerAction {

	final static boolean debug = false;

	public ImageProcessingAction() {
		super("Process Images for signs");
	}

	public void actionPerformed(ActionEvent arg0) {

		GeoImageLayer layer = getLayer();
		final List<ImageEntry> images = new ArrayList<ImageEntry>();
		List<ImageEntry> processedImages = new ArrayList<ImageEntry>();
		for (ImageEntry e : layer.getImages()){
			images.add(e);
		}


		if(Main.main.getCurrentDataSet() == null){
			JOptionPane.showMessageDialog(
					Main.parent,
					"Download from osm server. Or just use continous download plugin" ,
					"Err",
					JOptionPane.ERROR_MESSAGE
					);

		}
		else{
			final Processor process = new Processor();
			process.getImageEntryList(images);
			process.pack();
			process.setLocationRelativeTo(null);
			process.setVisible(true);
		}

	}

	static class VideoProcessingRunnable extends PleaseWaitRunnable {
		final private List<ImageEntry> images;
		private boolean canceled = false;

		public VideoProcessingRunnable(List<ImageEntry> images) {
			super("Videoprocessor");
			this.images = images;
		}

		@Override
		protected void realRun(){
			for (int i = 0; i < images.size(); ++i){
				if(canceled) return;
				ImageEntry e = images.get(i);
			}
		}

		@Override
		protected void finish() {
		}

		@Override
		protected void cancel() {
			canceled = true;
		}
	}

	private GeoImageLayer getLayer() {
		return (GeoImageLayer)LayerListDialog.getInstance()
				.getModel()
				.getSelectedLayers()
				.get(0);
	}

	private boolean enabled(GeoImageLayer layer) {
		for (ImageEntry e : layer.getImages()) {
			if (e.getPos() != null && e.getGpsTime() != null)
				return true;
		}
		return false;
	}

	public Component createMenuComponent() {
		JMenuItem geotaggingItem = new JMenuItem(this);
		geotaggingItem.setEnabled(enabled(getLayer()));
		return geotaggingItem;
	}

	public boolean supportLayers(List<Layer> layers) {
		return layers.size() == 1 && layers.get(0) instanceof GeoImageLayer;
	}


}
package org.openstreetmap.josm.plugins.imageprocessor;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
/**Provides dialog to display and interact with processed and sign
 * detected images
 * 
 * @author nikhil
 *
 */
public final class ProcessedFrames extends JFrame {

	private final JLabel imageView = new JLabel();
	private IplImage image = null;
	private int tempIndex;
	private int nextFrame;
	private int prevFrame;
	private int wayListSize;
	private LatLon latLon;
	double latlonBuffer = 0.000000001;
	MarkerLayer markerLayer;
	List<ImageEntry> imageList;
	ImageEntry imageEntry;
	Marker currentPos;

	/**
	 * Display anf interact with processed images and zoom map to location of images.
	 * @param CASCADE_FILE Sets name of trianing file as label of dialog
	 * @throws HeadlessException
	 */
	public ProcessedFrames(String CASCADE_FILE, MarkerLayer markerLayer) throws HeadlessException {
		super(CASCADE_FILE);
		this.markerLayer = markerLayer;
		Main.map.mapView.zoomToFactor(0.5);
		final Action nextAction = new AbstractAction(">") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (nextFrame == tempIndex){
					}
					else{
						nextImageEntry(imageList);
						final IplImage img = openImage();
						if (nextFrame == 0 || nextFrame == 1){
							prevFrame = 1;
						}
						else{
							prevFrame = nextFrame - 1;
						}
						nextFrame = nextFrame + 1;

						if (img != null) {
							image = img;
							imageView.setIcon(new
									ImageIcon(image.getBufferedImage()));
						}
						//Main.map.repaint();
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		final Action prevAction = new AbstractAction("<") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					prevImageEntry(imageList);
					final IplImage img = openImage();
					nextFrame = prevFrame + 1;
					if(prevFrame == 0){
					}
					else{
						prevFrame = prevFrame - 1;
					}
					if (img != null) {
						image = img;
						imageView.setIcon(new
								ImageIcon(image.getBufferedImage()));
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};




		final JPanel buttonsPanel = new JPanel(new GridLayout(0, 3, 0, 8));
		buttonsPanel.add(new JButton(prevAction));
		buttonsPanel.add(new JButton(nextAction));


		final JPanel botPanel = new JPanel();
		botPanel.add(buttonsPanel);
		add(botPanel, BorderLayout.SOUTH);


		final JPanel botPane = new JPanel();
		botPane.add(botPanel);
		add(botPane, BorderLayout.SOUTH);

		final JScrollPane imageScrollPane = new JScrollPane(imageView);
		imageScrollPane.setPreferredSize(new Dimension(640, 480));
		add(imageScrollPane, BorderLayout.CENTER);
	}
	/**
	 * Open image from imageEntry file path and returns as IplImage for display
	 * and image processing
	 * @return IplImage
	 */
	private IplImage openImage() {
		final String path = imageEntry.getFile().getAbsolutePath();
		if (imageEntry == null){
			showMessageDialog(this, "Cannot open image file: " + path, getTitle(), ERROR_MESSAGE);
			return null;
		}
		else{

			final IplImage newImage = cvLoadImage(path);

			if (newImage != null) {
				return newImage;
			} else {
				showMessageDialog(this, "Cannot open image file: "
						+ path, getTitle(), ERROR_MESSAGE);
				return null;
			}
		}
	}


	public void getImageEntryList(List<ImageEntry> imageList){
		this.imageList =  imageList;
		imageEntry = imageList.get(0);
		tempIndex = imageList.size();
		nextFrame = 1;
		prevFrame = 0;
		IplImage image = openImage();
		imageView.setIcon(new ImageIcon(image.getBufferedImage()));
	}

	/**
	 * Provides next image from List<ImageEntry> to IplImage image
	 * and sets marker at corresponding position
	 * @param imageList List<ImageEntry>
	 */

	private void nextImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(nextFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());
		markerLayer.data.clear();
		currentPos = new Marker(imageEntry.getPos().getRoundedToOsmPrecision(),
				"Current Position",	null, null, -1.0, 0.0);
		markerLayer.data.add(currentPos);
		/*
		latLon = imageEntry.getPos();
		Bounds bound = new Bounds(latLon.lat() - latlonBuffer, latLon.lon() - latlonBuffer,
				latLon.lat() + latlonBuffer, latLon.lon() + latlonBuffer);
		BBox bbox = new BBox(bound);

		List<Way> w = Main.main.getCurrentDataSet().searchWays(bbox);
		wayListSize = w.size();*/

		//Collection<WaySegment> ws = new Collection <WaySegment> (w.iterator().next(), 0);
		//Main.main.getCurrentDataSet().setHighlightedWaySegments(ws);
	}
	/**
	 * Provides previous image from List<ImageEntry> to IplImage image
	 * and sets marker at corresponding position
	 * @param imageList List<ImageEntry>
	 */
	private void prevImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(prevFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());
		markerLayer.data.clear();
		currentPos = new Marker(imageEntry.getPos().getRoundedToOsmPrecision(),
				"Current Position",	null, null, -1.0, 0.0);
		markerLayer.data.add(currentPos);
		//Main.map.mapView.zoomToFactor(0.5);
	}
}
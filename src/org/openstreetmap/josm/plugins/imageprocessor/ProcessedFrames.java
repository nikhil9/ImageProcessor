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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;

import com.googlecode.javacv.cpp.opencv_core.IplImage;


public final class ProcessedFrames extends JFrame {

	private final JLabel imageView = new JLabel();
	private IplImage image = null;

	private int tempIndex;
	private int nextFrame;
	private int prevFrame;
	private int wayListSize;
	private LatLon latLon;
	double latlonBuffer = 0.000000001;

	List<ImageEntry> imageList;
	ImageEntry imageEntry;


	public ProcessedFrames(String CASCADE_FILE) throws HeadlessException {
		super(CASCADE_FILE);

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
		//buttonsPanel.add(new JButton(processAction));

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



	private void nextImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(nextFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());


		latLon = imageEntry.getPos();
		Bounds bound = new Bounds(latLon.lat() - latlonBuffer, latLon.lon() - latlonBuffer,
				latLon.lat() + latlonBuffer, latLon.lon() + latlonBuffer);
		BBox bbox = new BBox(bound);

		List<Way> w = Main.main.getCurrentDataSet().searchWays(bbox);
		wayListSize = w.size();
		JOptionPane.showMessageDialog(
				Main.parent,
				"Total number = " + latLon  + ", "+ wayListSize,
				"Number of adjecent ways",
				JOptionPane.PLAIN_MESSAGE
				);

		Way w1 = w.iterator().next();


		//Collection<WaySegment> ws = new Collection <WaySegment> (w.iterator().next(), 0);
		//Main.main.getCurrentDataSet().setHighlightedWaySegments(ws);
	}

	private void prevImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(prevFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());
		//Main.map.mapView.zoomToFactor(0.5);
	}
}
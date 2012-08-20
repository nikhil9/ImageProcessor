package org.openstreetmap.josm.plugins.imageprocessor;

import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvEllipse;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CascadeClassifier;


public final class Processor extends JFrame {

	private final JFileChooser fileChooser = new JFileChooser();
	public String CASCADE_FILE = null;
	private final JLabel imageView = new JLabel();
	private IplImage image = null;

	private int tempIndex;
	private int nextFrame;
	private int prevFrame;
	private int wayListSize;
	private LatLon latLon;
	double latlonBuffer = 0.000000001;
	List<ImageEntry> imageList;
	GpxLayer gpxLayer;
	MarkerLayer markerLayer;
	ImageEntry imageEntry;
	Marker currentPos;
	public boolean setProcessButton;



	public Processor() throws HeadlessException {
		super("Image Processor");
		Main.map.mapView.zoomToFactor(0.15);

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
						Main.map.repaint();
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

		final Action processAction = new AbstractAction("Process") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if(CASCADE_FILE == null){
						JOptionPane.showMessageDialog(
								Main.parent,
								"Set Cascade file First",
								"Cascade File Not Found",
								JOptionPane.PLAIN_MESSAGE
								);
					}else{
						showProcessedImages(processImage(imageList));
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};


		final Action setCascadeFileAction = new AbstractAction("Set Cascade file") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					CASCADE_FILE = getCascadeFile();

				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};


		final JPanel buttonsPanel = new JPanel(new GridLayout(0, 4, 0, 8));
		buttonsPanel.add(new JButton(prevAction));
		buttonsPanel.add(new JButton(nextAction));
		buttonsPanel.add(new JButton(processAction));
		buttonsPanel.add(new JButton(setCascadeFileAction));

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


	private List<ImageEntry> processImage(List<ImageEntry> imgList) {
		List<ImageEntry> processedImageList = new ArrayList<ImageEntry>();
		for (int i = 0; i < imgList.size(); i++){
			IplImage src =
					cvLoadImage(imgList.get(i).getFile().getAbsolutePath());

			IplImage gray = cvCreateImage(cvGetSize(src), 8, 1);

			CvRect rects = new CvRect();
			rects.setNull();

			CascadeClassifier cascade = new CascadeClassifier();
			cascade.load(CASCADE_FILE);

			CvMemStorage storage = CvMemStorage.create();

			cvCvtColor(src, gray, CV_BGR2GRAY );

			cascade.detectMultiScale(src,
					rects,
					1.1,  // scale
					1,   // min neighbours
					0,
					cvSize(10, 10),
					cvSize(100, 100));

			cvClearMemStorage(storage);

			if(rects.isNull()){
				//processedImageList.add(imgList.get(i));
				//rects.setNull();
			}
			else{
				CvPoint center = new CvPoint(
						rects.x() + (rects.width()/2),
						rects.y() + (rects.height()/2));
				cvEllipse(src,
						center,
						cvSize(rects.width()/2, rects.height()/2),
						0,
						0,
						360,
						CvScalar.GREEN,
						4,
						8,
						0);
				processedImageList.add(imgList.get(i));
				rects.setNull();
			}
		}
		if (processedImageList.size() == 0){
			JOptionPane.showMessageDialog(
					Main.parent,
					"No image with given sign was found",
					"Image Processor",
					JOptionPane.ERROR_MESSAGE
					);
			return null;
		}
		else{

			JOptionPane.showMessageDialog(
					Main.parent,
					"Some image with given sign was found",
					"Image Processor",
					JOptionPane.ERROR_MESSAGE
					);
			return processedImageList;
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
		CreateMarkerLayer(imageEntry.getPos().getRoundedToOsmPrecision());



	}



	private void nextImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(nextFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());

		markerLayer.data.clear();
		currentPos = new Marker(imageEntry.getPos().getRoundedToOsmPrecision(),
				"Current Position",	null, null, -1.0, 0.0);
		markerLayer.data.add(currentPos);


		latLon = imageEntry.getPos();
		Bounds bound = new Bounds(latLon.lat() - latlonBuffer, latLon.lon() - latlonBuffer,
				latLon.lat() + latlonBuffer, latLon.lon() + latlonBuffer);
		BBox bbox = new BBox(bound);

		List<Way> w = Main.main.getCurrentDataSet().searchWays(bbox);
		wayListSize = w.size();

	}

	private void prevImageEntry(List<ImageEntry> imageList){
		imageEntry = imageList.get(prevFrame);
		Main.map.mapView.zoomTo(imageEntry.getPos().getEastNorth());
		//MarkerLayerHandler(imageEntry.getPos()
		//		.getRoundedToOsmPrecision());
		markerLayer.data.clear();
		currentPos = new Marker(imageEntry.getPos().getRoundedToOsmPrecision(),
				"Current Position",	null, null, -1.0, 0.0);
		markerLayer.data.add(currentPos);
	}

	public void showProcessedImages(List<ImageEntry> imgList){
		if(imgList != null){
			final ProcessedFrames t = new ProcessedFrames(CASCADE_FILE.toString());
			t.getImageEntryList(imgList);
			t.pack();
			t.setVisible(true);
		}

	}
	public String getCascadeFile(){
		if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		final String cascadePath = fileChooser.getSelectedFile().getAbsolutePath();
		return cascadePath;

	}

	public void CreateMarkerLayer(LatLon latlon){

		Collection<Layer> layers = Main.map.mapView.getAllLayers();

		for (Layer layer : layers){
			if(layer instanceof GpxLayer) {
				gpxLayer = (GpxLayer) layer;
				break;
			}
		}
		markerLayer = new MarkerLayer(gpxLayer.data,"ImageProcessor", null, null);
		Main.main.addLayer(markerLayer);
		markerLayer.setVisible(true);
		currentPos = new Marker(latlon, "Current Position",
				null, null, -1.0, 0.0);
		markerLayer.data.add(currentPos);


	}

}


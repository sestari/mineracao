package br.furb;

import java.io.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.SwingUtilities;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

public class View extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";
	private JButton openButton, exportButton;
	private JTextArea log;
	private JFileChooser fc;
	private Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	
	
	private ArrayList<int[]> colors = new ArrayList<int[]>();

	public View() {
		super(new BorderLayout());

		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		fc = new JFileChooser();

		openButton = new JButton("Abrir .MP4...");
		openButton.addActionListener(this);

		exportButton = new JButton("Exportar...");
		exportButton.addActionListener(this);

		JPanel buttonPanel = new JPanel(); 
		buttonPanel.add(openButton);
		buttonPanel.add(exportButton);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(View.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				log.append("Opening: " + file.getName() + "." + newline);

				tratarVideo(file);
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());

			// Handle save button action.
		} else if (e.getSource() == exportButton) {
			int returnVal = fc.showSaveDialog(View.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				// This is where a real application would save the file.
				log.append("Saving: " + file.getName() + "." + newline);

			} else {
				log.append("Save command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
	}
	
	
	private int[] getRGBImage(int x, int y, BufferedImage image){
		int clr =  image.getRGB(x,y); 
		int[] rets =  new int[3];
		
		rets[0] = (clr & 0x00ff0000) >> 16; //red
		rets[1] = (clr & 0x0000ff00) >> 8; //green
		rets[2] = clr & 0x000000ff; // blue
		log.append("r: " + rets[0]);
		log.append(" g: " + rets[1]);
		log.append(" b: " + rets[2]);
		log.append(newline);
		
		return rets;
	}

	private void tratarVideo(File file) {

		FFmpegFrameGrabber g = new FFmpegFrameGrabber(file);
		try {
			g.start();
			org.bytedeco.javacv.Frame frame = g.grabImage();
			
			while(frame != null){
				BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / g.getGamma());
				//ImageIO.write(image, "png", new File("/Users/andresestari/Desktop/teste/video-frame-" + System.currentTimeMillis() + ".png"));
				
				//pega 4 cores do frame
				colors.add(getRGBImage(40, 40, image));
				colors.add(getRGBImage(image.getHeight() -40, image.getHeight() - 40, image));
				colors.add(getRGBImage(40, image.getHeight() - 40, image));
				colors.add(getRGBImage(image.getHeight() -40, 40, image));
				
				frame = g.grabImage();
			}
			g.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = View.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new View());

		
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}
}
package br.furb;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

public class View extends JPanel implements ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";
	private JButton openButton, exportButton, resetButton;
	private JTextField qtFramesTF, corteTF;
	private JTextArea log;
	private JFileChooser fc;
	private JFileChooser fcExport;
	private JTable table;
	private DefaultTableModel dtm;
	private FileNameExtensionFilter filter = null;
	private Java2DFrameConverter paintConverter = new Java2DFrameConverter();

	private ArrayList<int[]> colors = new ArrayList<int[]>();

	public View() {
		super(new BorderLayout());

		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		table = new JTable();
		dtm = new DefaultTableModel(0, 0);
		String header[] = new String[] { "Nome", "R", "G", "B", "Variação", "Gênero" };
		dtm.setColumnIdentifiers(header);
		table.setModel(dtm);
		JScrollPane tableScrollPane = new JScrollPane(table);

		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setFileFilter(new FileNameExtensionFilter("Mp4 files", "mp4", "video"));

		fcExport = new JFileChooser();
		fcExport.setMultiSelectionEnabled(true);
		fcExport.setFileFilter(new FileNameExtensionFilter("XLS files", "xlsx", "xlsx"));

		openButton = new JButton("Abrir .MP4");
		openButton.addActionListener(this);

		exportButton = new JButton("Exportar");
		exportButton.addActionListener(this);
		
		qtFramesTF = new JTextField("1000");
		
		corteTF = new JTextField("1000");

		resetButton = new JButton("Apagar Tabela");
		resetButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(exportButton);
		buttonPanel.add(new JLabel("Índice frames"));
		buttonPanel.add(qtFramesTF);
		buttonPanel.add(new JLabel("Cortes finais"));
		buttonPanel.add(corteTF);
		

		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
		add(tableScrollPane, BorderLayout.EAST);
	}

	public void actionPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource().equals(openButton)) {
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(View.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] files = fc.getSelectedFiles();
				for (File file : files) {
					log.append("Opening: " + file.getName() + "." + newline);
					tratarVideo(file);
				}
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());

			// Handle save button action.
		} else if (e.getSource().equals(exportButton)) {
			if (dtm.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null, "Nenhum arquivo foi selecionado!");
			}
			int returnVal = fcExport.showSaveDialog(View.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fcExport.getSelectedFile();

				exportarExcel(file);
				// This is where a real application would save the file.
				log.append("Saving: " + file.getName() + "." + newline);
				JOptionPane.showMessageDialog(null, "Arquivo Exportado com sucesso!");

			} else {
				log.append("Save command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		} else if (e.getSource().equals(resetButton)) {
			dtm.setRowCount(0);
		}
	}

	private int[] getRGBImage(int x, int y, BufferedImage image) {
		int clr = image.getRGB(x, y);
		int[] rets = new int[3];

		rets[0] = (clr & 0x00ff0000) >> 16; // red
		rets[1] = (clr & 0x0000ff00) >> 8; // green
		rets[2] = clr & 0x000000ff; // blue
		log.append("r: " + rets[0]);
		log.append(" g: " + rets[1]);
		log.append(" b: " + rets[2]);
		log.append(newline);

		return rets;
	}

	private void exportarExcel(File arquivo) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet firstSheet = workbook.createSheet("Filmes");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(arquivo);
			HSSFRow row = firstSheet.createRow(0);
			row.createCell(0).setCellValue("Nome");
			row.createCell(1).setCellValue("R");
			row.createCell(2).setCellValue("G");
			row.createCell(3).setCellValue("B");
			row.createCell(4).setCellValue("Variação");
			row.createCell(5).setCellValue("Gênero");
			for (int x = 0; x < dtm.getRowCount(); x++) {
				row = firstSheet.createRow(x);
				for (int y = 0; y < 6; y++)
					row.createCell(y).setCellValue(dtm.getValueAt(x, y).toString());
			}
			workbook.write(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao exportar arquivo");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao exportar arquivo");
		} finally {
			try {
				workbook.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void tratarVideo(File file) {
		colors.clear();
		
		int indice = Integer.valueOf(qtFramesTF.getText());
		
		log.setText("");
		
		FFmpegFrameGrabber g = new FFmpegFrameGrabber(file);
		
		try {
			g.start();

			org.bytedeco.javacv.Frame frame = g.grabImage();

			Integer qtFrames = 0;

			
							
			Long inicioF = new Date().getTime();
			
			

			while (frame != null) {
				
				Long a = new Date().getTime();
				
				if(qtFrames >= indice){
					qtFrames = 0;
					frame = g.grabImage();
				

					BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / g.getGamma());
	
					if (image != null) {
	
						// pega 4 cores do frame
						int[] rgb1 = getRGBImage(40, 40, image);
						int[] rgb2 = getRGBImage(image.getHeight() - 40, image.getHeight() - 40, image);
						int[] rgb3 = getRGBImage(40, image.getHeight() - 40, image);
						int[] rgb4 = getRGBImage(image.getHeight() - 40, 40, image);
						
						colors.add(rgb1);
						colors.add(rgb2);
						colors.add(rgb3);
						colors.add(rgb4);
						
					}
					
				} else {
					Long b = new Date().getTime();
					frame = g.grabFrame(false);
				}
			
				qtFrames++;
			}
			
			
			System.out.println((new Date().getTime() - inicioF) / 1000d + "s");
			
			g.stop();
			VideoRGBV videoInfo = new VideoRGBV(colors, Integer.valueOf(corteTF.getText()));
			// coloca o nome, o genero, r, g, b e a variação de frame

			dtm.addRow(new Object[] { file.getName(), videoInfo.red, videoInfo.green, videoInfo.blue,
					videoInfo.variacao, "" });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
			 * catch (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
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
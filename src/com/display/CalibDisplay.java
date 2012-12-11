package com.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import util.Data;

import com.detector.CalibrationData;

public class CalibDisplay extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3359575825949376310L;
	
	private CalibrationData calibrationData;
	private JTextArea dataBox;
	private JTextField channelBox;
	
	private final static String USAGE = "USAGE:\n" +
										"======\n" +
										"Enter channel numbers separated by commas or spaces," +
										"or an asterisk as a wildcard.\nRanges may be specified" +
										"using any of the following forms: START-END, START:END," +
										"or START..END\n" +
										"Ranges with a wildcard expand: START-* means START-(final channel)";
	
	public CalibDisplay(CalibrationData cd) {
		super("Calibration Data");
		calibrationData = cd;
		setup();
	}

	public CalibDisplay(String title, CalibrationData cd) {
		super(title);
		calibrationData = cd;
		setup();
	}
	
	public void setup() {
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		this.setSize(new Dimension(345, 600));
		this.setMinimumSize(new Dimension(345, 450));
		this.setLayout(new BorderLayout());
		
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		selectionPanel.setPreferredSize(new Dimension(345, 24));
		
		JLabel channelLabel = new JLabel("Channel:");
		channelLabel.setPreferredSize(new Dimension(60, 18));
		channelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		channelBox = new JTextField(20);
		channelBox.setPreferredSize(new Dimension(60, 10));
		channelBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (calibrationData == null) {
					dataBox.setText("Please choose a calibration file.\n");
					return;
				}
				int[][] x = Data.parseRange(channelBox.getText());
				String t = "";
				for (int[] r : x) {
					if (r.length == 1) {
						if (r[0] == -1) {
							for (int i = 0; i < calibrationData.getChannelCount(); i++) {
								t += "Channel "+String.valueOf(i)+":\n";
								for (int b = 0; b < 4; b++) {
									t += formatData(i, b);
								}
							}
							return;
						} else {
							t += "Channel "+String.valueOf(r[0])+":\n";
							for (int b = 0; b < 4; b++) {
								t += formatData(r[0], b);
							}
						}
					} else {
						int start = r[0];
						int end = r[1] == -1 ? calibrationData.getChannelCount() : r[1]+1;
						for (int i = start; i < end; i++) {
							t += "Channel "+String.valueOf(i)+":\n";
							for (int b = 0; b < 4; b++) {
								t += formatData(i,b);
							}
						}
					}
					
				}
				dataBox.setText(t);
				
			}
		});
		
		dataBox = new JTextArea(USAGE);
		dataBox.setFont(new Font("monospaced", Font.PLAIN, 12));
		dataBox.setFocusable(false);
		dataBox.setLineWrap(true);
		dataBox.setTabSize(4);
		dataBox.setWrapStyleWord(true);
		dataBox.setMinimumSize(new Dimension(325, 400));
		dataBox.setOpaque(false);
		dataBox.setBorder(new EmptyBorder(8, 8, 8,8));
		JScrollPane dataPanel = new JScrollPane(dataBox);
		
		selectionPanel.add(channelLabel, BorderLayout.WEST);
		selectionPanel.add(channelBox, BorderLayout.EAST);
		
		this.add(selectionPanel, BorderLayout.NORTH);
		this.add(dataPanel, BorderLayout.CENTER);
		this.setVisible(true);
		
	}
	public String formatData(int channel, int bucket) {
		if (calibrationData == null) return "";
		Float[]	c = calibrationData.getData(channel, bucket);
		String t = "";
		if (c != null) {
			t+= "\tb"+String.valueOf(bucket)+":";
//			t+= String.valueOf(c[0])+'\n';
			t+= "{bm:";
//			t+= String.valueOf(calibrationData.calibrate(i, b, c[0]))+'\n';
			t+= String.valueOf(c[0]);
			t+= ", cg:";
			t+= String.valueOf(c[6]);
			t+= ", ci:";
			t+= String.valueOf(c[7])+"}\n";
		}
		return t;
	}
	public void setCalibData(CalibrationData cd) {
		calibrationData = cd;
	}
	public static void main(String[] args) {
		try {
			new CalibDisplay("Calibration", args.length > 0 ? CalibrationData.getInstance(args[0]) : null);
		} catch (Exception e) {
			System.out.println("Could not open calibration file: '" + args[0] + "'.");
			new CalibDisplay("Calibration", null);
		}
	}
}

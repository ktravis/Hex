package com.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import util.Data;
import util.NumComparator;

import com.detector.HexDetector;

public class GUIBar extends JToolBar {
	private static final long serialVersionUID = -1085945138422149297L;

	private HexDetector h;
	private JTable dataTable;
	private JPanel filePanel, displayPanel, dropdownPanel, sliderPanel, playPanel, speedPanel, labelPanel, configPanel;
	private JButton browse, calibrate, reset, rew, pp, step, config, saveConf; 
	private FloatSlider coeff;
	private JComboBox<String> displayMenu, labelMenu;
	private JSlider speed;
	private JLabel fileLabel, calibFileLabel, cLabel, sLabel;
	private JScrollPane dataPane;
	private JCheckBox liveUpdateBox, adjustedCheckBox, labelCheckBox;
	private DefaultTableModel model;
	
	private boolean adjusted = false;
	private boolean updateLive = true;

	
	
	public GUIBar(HexDetector d) {
		super(JToolBar.VERTICAL);
		h = d;
		setup();
	}
	
	@SuppressWarnings({ "unchecked" })
	public void setup() {
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.setFloatable(false);
		this.setPreferredSize(new Dimension(180, GLWindow.DISPLAY_HEIGHT));

		displayMenu = new JComboBox<String>(new String[]{"Absolute", "Calibrated"});
		
		fileLabel = new JLabel("File: No file selected.");
		fileLabel.setSize(new Dimension(180, 30));
		fileLabel.setFont(Data.getFont(10));
		browse = new JButton("Browse...");
		browse.setFocusable(false);
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(new JFrame(), "Import...", FileDialog.LOAD);
				fd.setFile("*.bin");
				fd.requestFocus();
				fd.setVisible(true);
				if (fd.getFile() != null) {
					h.setKpixReader(Data.readKpixDataFile(fd.getDirectory()+fd.getFile()));
					fileLabel.setText("File: " + fd.getFile());
					update();
					displayMenu.setSelectedItem("Absolute");
				}
			} 
		});
		
		calibFileLabel = new JLabel("Calib: No file selected.");
		calibFileLabel.setSize(new Dimension(180, 30));
		calibFileLabel.setFont(Data.getFont(10));
		calibrate = new JButton("Calibrate");
		calibrate.setFocusable(false);
		calibrate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				h.calibrateData();
				calibFileLabel.setText("Calib: " + h.currFileName());
				update();
				displayMenu.setSelectedItem("Calibrated");
			} 
		});
		
		filePanel = new JPanel();
		filePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		browse.setMargin(new Insets(2, 1, 1, 2));
		calibrate.setMargin(new Insets(2, 1, 1, 2));
		browse.setFont(Data.getFont(10));
		calibrate.setFont(Data.getFont(10));
		
		filePanel.add(browse);
		filePanel.add(calibrate);
		this.add(fileLabel);
		this.add(calibFileLabel);
		
		dropdownPanel = new JPanel();
		dropdownPanel.setLayout(new BoxLayout(dropdownPanel, BoxLayout.Y_AXIS));
		
		displayMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String sel = (String)displayMenu.getSelectedItem();
				if (!h.isCalibrated()) {
					displayMenu.setSelectedIndex(0);
					return;
				}
				if (sel.contains("Absolute")) h.setDispMode(0);
				else if (sel.contains("Calibrated") ) h.setDispMode(1);
			}
		});
		
		JLabel displayLabel = new JLabel("Display mode: ");
		displayLabel.setPreferredSize(new Dimension(80, 10));
		displayLabel.setFont(Data.getFont(10));
		displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		displayPanel.add(displayLabel, BorderLayout.WEST);
		displayPanel.add(displayMenu, BorderLayout.CENTER);
		dropdownPanel.add(displayPanel);
		
		labelMenu = new JComboBox<String>(new String[] {"delta", "ADC", "% delta", "Indices", "ADC - min"});
		labelMenu.addActionListener(new ActionListener() {
			int last = 1;
			@Override
			public void actionPerformed(ActionEvent e) {
				int curr = labelMenu.getSelectedIndex();
				if (!h.isCalibrated() && (curr != 1 && curr != 3)) labelMenu.setSelectedIndex(last);
				else {
					last = curr;
					h.setLabelMode(curr);
				}
			}
		});
		labelMenu.setSelectedIndex(1);
		
		JLabel labelsLabel = new JLabel("Label mode: ");
		labelsLabel.setPreferredSize(new Dimension(80, 10));
		labelsLabel.setFont(Data.getFont(10));
		labelPanel = new JPanel();
		
		for (JComboBox<String> cb : new JComboBox[]{displayMenu, labelMenu}) {
			cb.setFont(Data.getFont(10));
			cb.setFocusable(false);
			cb.setPreferredSize(new Dimension(80, 15));
		}
		
		labelPanel.add(labelsLabel, BorderLayout.WEST);
		labelPanel.add(labelMenu, BorderLayout.CENTER);
		dropdownPanel.add(labelPanel);
		
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		coeff = new  FloatSlider(0, 200, 50, 100);
		cLabel = new JLabel("Scale coeff. : 0.5");
		coeff.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				h.setScale(coeff.getScaled());
				cLabel.setText("Scale coeff. : " + String.valueOf(coeff.getScaled()));
			}
		});
		coeff.setFocusable(false);
		sliderPanel.add(coeff);
		sliderPanel.add(cLabel);
		
		speedPanel = new JPanel();
		speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS));
		speed = new  JSlider(0, 100, 100);
		sLabel = new JLabel("Playspeed : 100%");
		speed.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				h.setPlayspeed(100.0f/speed.getValue());
				sLabel.setText("Playspeed : " + String.valueOf(speed.getValue()) + "%");
			}
		});
		speed.setFocusable(false);
		speedPanel.add(speed);
		speedPanel.add(sLabel);
		
		playPanel = new JPanel();
		playPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		reset = new JButton("<<");
		reset.setMaximumSize(new Dimension(30, 15));
		rew = new JButton("<");
		rew.setMaximumSize(new Dimension(30, 15));
		pp = new JButton("Play");
		pp.setMaximumSize(new Dimension(45, 15));
		step = new JButton(">");
		step.setMaximumSize(new Dimension(30, 15));
		reset.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.resetData();
				update();
			}
		});
		rew.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.stepDataBack();
				update();
			}
		});
		pp.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.togglePlaying();
				pp.setText(h.isPlaying() ? "Pause" : "Play");
			}
		});
		step.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.stepData();
				update();
			}
		});
		for (JButton b : new JButton[] {reset, rew, pp, step}) {
			b.setFocusable(false);
			b.setFont(Data.getFont(10));
			b.setPreferredSize(new Dimension(40, 20));
			b.setMargin(new Insets(2, 1, 1, 2));
			playPanel.add(b);
		}
		
		model = new DefaultTableModel(new Object[]{"index", "ADC", "time"}, 0) {
			private static final long serialVersionUID = 1L;
			@Override public boolean isCellEditable(int row, int column) { return false; }
		};
		dataTable = new JTable(model);
		float[] data = h.getData();
		float[] min = h.getMins();
//		float[] mean = h.getMeans();
		int[] time = h.getTimes();
		for (int i = 0; i < 1024; i++) {
			model.addRow(new Object[]{i, data[i], time[i]});
		}
		dataTable.addMouseMotionListener(new MouseMotionListener() {
			int last = 0;
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = dataTable.rowAtPoint(e.getPoint());
				if (row != last) h.highlightPixel((Integer) dataTable.getValueAt(row, 0));
				last = row;
			}
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		dataTable.setFillsViewportHeight(true);
		dataTable.setAutoCreateRowSorter(false);
		dataTable.setFocusable(false);
		TableRowSorter<DefaultTableModel> trs = new TableRowSorter<DefaultTableModel>(model);
		for (int i = 0; i < 3; i++) {
			trs.setComparator(i, i > 0 ? new NumComparator<Float>() : new NumComparator<Integer>());
			trs.setComparator(2, new NumComparator<Integer>());
		}
		dataTable.setRowSorter(trs);
		dataPane = new JScrollPane(dataTable);
		
		for (JPanel p : new JPanel[] {filePanel, dropdownPanel, sliderPanel, playPanel, speedPanel, }) {
			p.setPreferredSize(new Dimension(180, 30));
			p.setFocusable(false);
			this.add(p);
		}
		dataPane.setFocusable(false);
		dataPane.setPreferredSize(new Dimension(180, 300));
		dropdownPanel.setPreferredSize(new Dimension(180, 40));
		playPanel.setPreferredSize(new Dimension(180, 30));
		
		this.add(dataPane);
		
		liveUpdateBox = new JCheckBox("live");
		liveUpdateBox.setSelected(updateLive);
		liveUpdateBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateLive = e.getStateChange() == ItemEvent.SELECTED ? true : false;
				h.updateLive = updateLive;
			}
		});
		this.add(liveUpdateBox);
		
		adjustedCheckBox = new JCheckBox("adjust");
		adjustedCheckBox.setSelected(adjusted);
		adjustedCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				adjusted = e.getStateChange() == ItemEvent.SELECTED ? true : false;
				h.adjusted = adjusted;
				update();
			}
		});
		this.add(adjustedCheckBox);
		
		labelCheckBox = new JCheckBox("labels");
		labelCheckBox.setSelected(false);
		labelCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (h.labels() == (e.getStateChange() == ItemEvent.SELECTED)) return;
				else h.toggleLabels();
			}
		});
		this.add(labelCheckBox);
		for (JCheckBox c : new JCheckBox[] {liveUpdateBox, adjustedCheckBox, labelCheckBox}) {
			c.setFont(Data.getFont(10));
			c.setMargin(new Insets(1, 2, 1, 2));
		}
		
		configPanel = new JPanel();
		configPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		config = new JButton("configure");
		config.setFocusable(false);
		config.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(new JFrame(), "Load configuration...", FileDialog.LOAD);
				fd.setFile("*.config");
				fd.requestFocus();
				fd.setVisible(true);
				String path = fd.getDirectory()+fd.getFile();
				if (path == null || path.contains("nullnull")) return;
				String[] lines = Data.fileRead(path);
				configure(lines);
				h.sendMessage("Loaded config file '" + fd.getFile() + "'!");
			}
		});
		saveConf = new JButton("save");
		saveConf.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(new JFrame(), "Save configuration...", FileDialog.SAVE);
				fd.setFile("*.config");
				fd.requestFocus();
				fd.setVisible(true);
				if (fd.getDirectory() != null && fd.getFile() != null) h.saveConfig(fd.getDirectory()+fd.getFile());
			}
		});
		for (JButton b : new JButton[] {config, saveConf}) {
			b.setFont(Data.getFont(10));
			b.setMargin(new Insets(1, 2, 1, 2));
			b.setFocusable(false);
			configPanel.add(b);
		}
		configPanel.setFocusable(false);
		this.add(configPanel);
		
		for (JLabel l : new JLabel[]{fileLabel, calibFileLabel, displayLabel, labelsLabel, cLabel, sLabel}) {
			l.setFocusable(false);
			l.setFont(Data.getFont(10));
		}
		
		final JColorChooser jcc = new JColorChooser();
		jcc.setChooserPanels(new AbstractColorChooserPanel[] {jcc.getChooserPanels()[1]});
		jcc.setPreviewPanel(new JPanel());
		
		JRadioButton lowRadio = new JRadioButton("Low");
		JRadioButton highRadio = new JRadioButton("High");
		JRadioButton zeroRadio = new JRadioButton("Zero");
		JButton resetColor = new JButton("Reset");
		resetColor.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				h.setColor(0, Color.blue);
				h.setColor(1, Color.red);
				h.setColor(2, Color.green);
			}
		});
		
		final ButtonGroup radios = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			JRadioButton b = new JRadioButton[]{lowRadio, highRadio, zeroRadio}[i];
			radios.add(b);
			b.setFocusable(false);
			final int index = i;
			b.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					jcc.setColor(h.getColor(index));
				}
			});
		}
		
		jcc.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int i = 0;
				for (Enumeration<AbstractButton> b = radios.getElements(); b.hasMoreElements();) {
					JRadioButton rb = (JRadioButton) b.nextElement();
					if (rb.isSelected()) break;
					i++;
				}
				h.setColor(i, jcc.getColor());
			}
		});
		lowRadio.setSelected(true);
		
		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
		colorPanel.add(lowRadio);
		colorPanel.add(highRadio);
		colorPanel.add(zeroRadio);
		colorPanel.add(resetColor);
		resetColor.setFocusable(false);
		colorPanel.setPreferredSize(new Dimension(100, 100));
		
		final JDialog jccd = new JDialog(new JFrame(), "Choose a color...");
		jccd.setLayout(new FlowLayout());
		jccd.add(colorPanel, FlowLayout.LEFT);
		jccd.add(jcc, FlowLayout.CENTER);
		jccd.pack();
		jccd.setMinimumSize(jccd.getSize());
		
		JButton cc = new JButton("COL");
		cc.setFocusable(false);
		cc.setFont(Data.getFont(10));
		cc.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				int i = 0;
				for (Enumeration<AbstractButton> b = radios.getElements(); b.hasMoreElements();) {
					JRadioButton rb = (JRadioButton) b.nextElement();
					if (rb.isSelected()) break;
					i++;
				}
				Color c = h.getColor(i);
				jcc.setColor(c);
				jccd.setVisible(true);
			}
		});
		this.add(cc);
		
		//
		if (Data.fileExists("res/def.config")) {
			configure(Data.fileRead("res/def.config"));
			h.sendMessage("Loaded config file 'def.config'!");
		}
	}
	
	public void update() { 
		float[]	data = h.getData();
		float[] min = h.getMins();
//		float[] mean = h.getMeans();
		int[] time = h.getTimes();

		for (int i = 0; i < 1024; i++) {
			int index = (Integer) model.getValueAt(i, 0);
			model.setValueAt(adjusted ? data[index] - min[index] : data[index], i, 1);
//			model.setValueAt(min[index], i, 2);
//			model.setValueAt(adjusted ? mean[index] - min[index] : mean[index], i, 3);
			model.setValueAt(time[index], i, 2);
		}
	}
	
	public void configure(String[] config) {
		for (String l : config) {
			String[] p = l.split(":", 2);
			try {
			if (p[0].contains("browse")) {
				if (!p[1].contains("null")) h.setKpixReader(Data.readKpixDataFile(p[1].trim()));
				fileLabel.setText("File: " + (h.currFileName() == null ? "No file selected." : h.currFileName()));
			}
			if (p[0].contains("calibrate")) if (p[1].contains("true") && h.currFileName() != null) calibrate.doClick();
			if (p[0].contains("scale")) coeff.setValue((int)(Float.valueOf(p[1])*coeff.scale));
			if (p[0].contains("update-live")) liveUpdateBox.setSelected(p[1].contains("true"));
			if (p[0].contains("adjust")) adjustedCheckBox.setSelected(p[1].contains("true"));
			if (p[0].contains("labels")) labelCheckBox.setSelected(p[1].contains("true"));
			if (p[0].contains("label-type")) labelMenu.setSelectedItem(p[1].trim());
			if (p[0].contains("display")) displayMenu.setSelectedIndex(p[1].contains("calib") ? 1 : 0);
			if (p[0].contains("speed")) speed.setValue(Integer.valueOf(p[1]));
			if (p[0].contains("zoom")) h.setZoom(Float.valueOf(p[1]));
			if (p[0].contains("axis")) h.setAxisPosition(-Float.valueOf(p[1].split(",")[0]), Float.valueOf(p[1].split(",")[1]));
			if (p[0].contains("index")) h.seek(Integer.valueOf(p[1].trim()));
			} catch (NumberFormatException n) {
			}
		}
	}
	
	private class FloatSlider extends JSlider {
		private static final long serialVersionUID = -5143507196060120764L;
		public float scale;
		public FloatSlider (int min, int max, int val, int scale) {
			super(min, max, val);
			this.scale = scale;
		}
		public float getScaled() {
			return getValue()/scale;
		}
	}
}

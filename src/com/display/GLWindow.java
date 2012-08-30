package com.display;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import util.Data;
import util.KeyHandler;
import util.MouseHandler;

import com.detector.HexDetector;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.Screenshot;

import java.awt.BorderLayout;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;


public class GLWindow extends JFrame {
	public static final int DISPLAY_WIDTH = 720;
	public static final int DISPLAY_HEIGHT = 540;
	GLProfile glProfile; 
	GLCapabilities glCapabilities;
	GLCanvas glCanvas;
	String[] dataString;
	JTable dataTable;
	
	HexDetector h;
	boolean capturing = false;
	boolean wasPlaying = false;
	boolean updateLive = true;
	File currCapture;
	
	public GLWindow(String title) { 
		super(title);
		
		glProfile = GLProfile.getDefault();
		glCapabilities = new GLCapabilities(glProfile);
		glCanvas = new GLCanvas(glCapabilities);
		
		setup();
	}
	
	void setup() {
		glCanvas.addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                Screen.setup( glautodrawable.getGL().getGL2(), width, height );
            }
            
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
                Screen.render( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight(), h);
                
                if (capturing) {
                	try {
                		System.out.print(String.format("Capturing file '%s'...", currCapture.toString()));
                		h.sendMessage(String.format("Capturing file '%s'...", currCapture.toString()));
						if (!Data.NO_TGA) {
							Screenshot.writeToTargaFile(currCapture, glCanvas.getWidth(), glCanvas.getHeight());
						} else {
							Screenshot.writeToFile(currCapture, glCanvas.getWidth(), glCanvas.getHeight());
						}
					} catch (GLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
                	capturing = false;
                	System.out.print(" done.\n");
                	h.sendMessage(String.format("Capturing file '%s'... done!", currCapture.toString()));
                }
                
                if (h.isPlaying() && updateLive) updateGUI();
                else if (!h.isPlaying() && wasPlaying) updateGUI();
                
                wasPlaying = h.isPlaying();
            }
        });
		
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		
		h = new HexDetector();
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		JPanel eastPanel = new JPanel();
		
		glCanvas.setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
		
		centerPanel.add(glCanvas, BorderLayout.CENTER);
		centerPanel.setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		eastPanel.add(setupToolBar());
		eastPanel.setFocusable(false);
		eastPanel.setPreferredSize(new Dimension(190, DISPLAY_HEIGHT));
		mainPanel.add(eastPanel, BorderLayout.LINE_END);
		
		this.setMinimumSize(new Dimension(DISPLAY_WIDTH + 200, DISPLAY_HEIGHT + 45));
		this.add(mainPanel);
		this.pack();

		
		MouseHandler inputHandler = new MouseHandler(h);
		new AWTMouseAdapter(inputHandler).addTo(glCanvas);
		
		KeyHandler keyHandler = new KeyHandler(this, h);
		new AWTKeyAdapter(keyHandler).addTo(glCanvas);
		
		FPSAnimator animator = new FPSAnimator(glCanvas, 60);
		animator.add(glCanvas);
		animator.start();
		
		this.setVisible(true);
		glCanvas.requestFocusInWindow();
	}
	
	public void destroy() {
		this.dispose();
		System.exit(0);
	}
	
	public boolean capture(boolean checkDir) {
		currCapture = Data.getCaptureFile(checkDir);
		if (currCapture != null) {
			capturing = true;
			return true;
		} else {
			System.out.println("Cannot save file to capture directory.");
			h.sendMessage("Cannot save file to capture directory.");
		}
		return false;
	}
	
	public JToolBar setupToolBar() {
		JToolBar bar = new JToolBar();
		bar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		bar.setFloatable(false);
		bar.setPreferredSize(new Dimension(180, DISPLAY_HEIGHT));

		final JComboBox displayMenu = new JComboBox<String>(new String[]{"Absolute", "Calibrated"});
		
		final JLabel fileLabel = new JLabel("File: No file selected.");
		fileLabel.setSize(new Dimension(180, 30));
		fileLabel.setFont(Data.getFont(10));
		JButton browse = new JButton("Browse...");
		browse.setFocusable(false);
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(new JFrame(), "Import...", FileDialog.LOAD);
				fd.setAutoRequestFocus(true);
				fd.setFile("*.bin");
				fd.setVisible(true);
				if (fd.getFile() != null) h.setKpixReader(Data.readKpixDataFile(fd.getDirectory()+fd.getFile()));
				fileLabel.setText("File: " + fd.getFile());
				updateGUI();
				displayMenu.setSelectedItem("Absolute");
			} 
		});
		
		final JLabel calibFileLabel = new JLabel("Calib: No file selected.");
		calibFileLabel.setSize(new Dimension(180, 30));
		calibFileLabel.setFont(Data.getFont(10));
		JButton calibrate = new JButton("Calibrate");
		calibrate.setFocusable(false);
		calibrate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				h.calibrateData();
				calibFileLabel.setText("Calib: " + h.currFileName());
				displayMenu.setSelectedItem("Calibrated");
			} 
		});
		
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		browse.setMargin(new Insets(2, 1, 1, 2));
		calibrate.setMargin(new Insets(2, 1, 1, 2));
		
		filePanel.add(browse);
		filePanel.add(calibrate);
		bar.add(fileLabel);
		bar.add(calibFileLabel);
		
		JPanel dropdownPanel = new JPanel();
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
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		displayPanel.add(displayLabel, BorderLayout.WEST);
		displayPanel.add(displayMenu, BorderLayout.CENTER);
		dropdownPanel.add(displayPanel);
		
		final JComboBox labelMenu = new JComboBox<String>(new String[] {"delta", "ADC", "% delta", "Indices", "ADC - min"});
		labelMenu.addActionListener(new ActionListener() {
			int last = 1;
			@Override
			public void actionPerformed(ActionEvent e) {
				int curr = ((JComboBox)e.getSource()).getSelectedIndex();
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
		JPanel labelPanel = new JPanel();
		
		for (JComboBox cb : new JComboBox[]{displayMenu, labelMenu}) {
			cb.setFont(Data.getFont(10));
			cb.setFocusable(false);
			cb.setPreferredSize(new Dimension(80, 15));
		}
		
		labelPanel.add(labelsLabel, BorderLayout.WEST);
		labelPanel.add(labelMenu, BorderLayout.CENTER);
		dropdownPanel.add(labelPanel);
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		final FloatSlider coeff = new  FloatSlider(0, 200, 50, 100);
		final JLabel cLabel = new JLabel("Scale coeff. : 0.5");
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
		
		JPanel speedPanel = new JPanel();
		speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS));
		final JSlider speed = new  JSlider(0, 100, 100);
		final JLabel sLabel = new JLabel("Playspeed : 100%");
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
		
		JPanel playPanel = new JPanel();
		playPanel.setLayout(new BoxLayout(playPanel, BoxLayout.X_AXIS));
		final JButton reset = new JButton("<<");
		reset.setMaximumSize(new Dimension(30, 30));
		final JButton rew = new JButton("<");
		rew.setMaximumSize(new Dimension(30, 30));
		final JButton pp = new JButton("Play");
		pp.setMaximumSize(new Dimension(45, 30));
		final JButton step = new JButton(">");
		step.setMaximumSize(new Dimension(30, 30));
		reset.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.resetData();
				updateGUI();
			}
		});
		rew.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				h.stepDataBack();
				updateGUI();
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
				updateGUI();
			}
		});
		for (JButton b : new JButton[] {reset, rew, pp, step}) {
			b.setFocusable(false);
			b.setFont(Data.getFont(12));
			b.setMargin(new Insets(2, 1, 1, 2));
			playPanel.add(b);
		}
		
		DefaultTableModel model = new DefaultTableModel(new Object[]{"index", "ADC"}, 0);
		dataTable = new JTable(model);
		float[] data = h.getData();
		for (int i = 0; i < 1024; i++) {
			model.addRow(new Object[]{i, data[i]});
		}
		dataTable.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = dataTable.rowAtPoint(e.getPoint());
				h.highlightPixel((Integer) dataTable.getValueAt(row, 0));
			}
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		dataTable.setFillsViewportHeight(true);
		dataTable.setAutoCreateRowSorter(true);
		JScrollPane dataPane = new JScrollPane(dataTable);
		
		
		for (JPanel p : new JPanel[] {filePanel, dropdownPanel, sliderPanel, playPanel, speedPanel, }) {
			p.setPreferredSize(new Dimension(180, 30));
			p.setFocusable(false);
			bar.add(p);
		}
		dataPane.setFocusable(false);
		dataPane.setPreferredSize(new Dimension(180, 300));
		dropdownPanel.setPreferredSize(new Dimension(180, 40));
		playPanel.setPreferredSize(new Dimension(180, 20));
		
		bar.add(dataPane);
		
		JCheckBox liveUpdateBox = new JCheckBox("update live");
		liveUpdateBox.setSelected(updateLive);
		liveUpdateBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateLive = e.getStateChange() == e.SELECTED ? true : false;
			}
		});
		bar.add(liveUpdateBox);
		
		JCheckBox labelCheckBox = new JCheckBox("labels");
		labelCheckBox.setSelected(false);
		labelCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (h.labels() == (e.getStateChange() == e.SELECTED)) return;
				else h.toggleLabels();
			}
		});
		bar.add(labelCheckBox);
		
		
		for (JLabel l : new JLabel[]{fileLabel, calibFileLabel, displayLabel, labelsLabel, cLabel, sLabel}) {
			l.setFocusable(false);
			l.setFont(Data.getFont(10));
		}
		
		return bar;
	}
	
	private class FloatSlider extends JSlider {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5143507196060120764L;
		float scale;
		public FloatSlider (int min, int max, int val, int scale) {
			super(min, max, val);
			this.scale = scale;
		}
		public float getScaled() {
			return getValue()/scale;
		}
	}
	
	public void updateGUI() { 
		float[]	data = h.getData();
		DefaultTableModel m = (DefaultTableModel) dataTable.getModel();
		for (int i = 0; i < 1024; i++) {
			int index = (Integer) m.getValueAt(i, 0);
			m.setValueAt(data[index], i, 1);
		}
	}
	
	public static void main(String[] args) {
		
		GLWindow s;
		if (args.length > 0) {
			if (args[0].startsWith("-")) {
				if (args[0].contains("--usage")) {
					System.out.println("java -jar EventDisplay.jar [-b[c] 'path/datafile.bin']");
				} else if (args[0].contains("b")) {
					if (args.length < 2) {
						System.out.println("java -jar EventDisplay.jar [-b[c] 'path/datafile.bin']");
					} else {
						s = new GLWindow("Event Display");
						s.h.setKpixReader(Data.readKpixDataFile(args[1]));
						if (args[0].contains("c")) s.h.calibrateData();
					}
				}
			} else {
				s = new GLWindow("Event Display");
				s.h.setData(Data.parseData(args[0]));
			}
		} else {
			s = new GLWindow("Event Display");
		}
		
	}
	
}

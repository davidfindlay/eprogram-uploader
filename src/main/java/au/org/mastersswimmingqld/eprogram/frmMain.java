package au.org.mastersswimmingqld.eprogram;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import com.jgoodies.forms.layout.*;

import javax.swing.JPasswordField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.Font;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JProgressBar;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.*;
import java.awt.event.*;

import static java.lang.String.*;


public class frmMain extends JFrame {
	
	private static final Logger log = Logger.getLogger( frmMain.class.getName() );
	
	private String filePath;

    static private Preferences preferences = Preferences.userRoot().node("/au/org/mastersswimmingqld/eprogram/uploader");

    SwingWorker<Boolean, Integer> worker;
    SwingWorker<Boolean, Integer> workerInstant;

    private Uploader uploader;
    private MeetList meets;
	boolean uploaderStatus = false;
    int defaultSeconds = 60;

	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField passwordField;
	private JLabel lblGroupLabel;
	private JLabel lblMMSection;
	private JLabel lblFileChooser;
	private JTextField txtDataFile;
	private JButton btnChooseFile;
	private JLabel lblUploadInterval;
	private JSpinner spnInterval;
	private JLabel lblMeetName;
	private JComboBox cmbMeetName;
	private JLabel lblStatus;
	private JLabel lblLastUpload;
	private JButton btnStartStop;
	private JButton btnUploadNow;
	private JButton btnExit;
	private JTextField txtLastUpload;
	private JLabel lblUploadProgress;
	private JProgressBar progressBar;
	private long interval;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmMain frame = new frmMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

    static void renderSplashFrame(Graphics2D g, int frame) {
        final String[] comps = {"foo", "bar", "baz"};
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString("Loading "+comps[(frame/5)%3]+"...", 120, 150);
    }

	/**
	 * Create the frame.
	 */
	public frmMain() {

//        setVisible(false);
//
//        final SplashScreen splash = SplashScreen.getSplashScreen();
//        if (splash == null) {
//            System.out.println("SplashScreen.getSplashScreen() returned null");
//            return;
//        }
//        Graphics2D g = splash.createGraphics();
//        if (g == null) {
//            System.out.println("g is null");
//            return;
//        }
//
//        for(int i=0; i<100; i++) {
//            renderSplashFrame(g, i);
//            splash.update();
//            try {
//                Thread.sleep(90);
//            }
//            catch(InterruptedException e) {
//            }
//        }

        // Get the default file path
        filePath = preferences.get("filePath", "");

        // Get the list of meets
        meets = new MeetList();

		setTitle("MSQ eProgram Upload Tool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 604, 379);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		lblGroupLabel = new JLabel("MSQ Members Community Login:");
		lblGroupLabel.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 15));
		contentPane.add(lblGroupLabel, "2, 2, 3, 1");
		
		JLabel lblUsername = new JLabel("Username:");
		contentPane.add(lblUsername, "2, 4, right, default");
		
		txtUsername = new JTextField();
		contentPane.add(txtUsername, "4, 4, left, default");
		txtUsername.setColumns(10);
        txtUsername.setText(preferences.get("username", ""));
		
		JLabel lblPassword = new JLabel("Password:");
		contentPane.add(lblPassword, "2, 6, right, default");
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		contentPane.add(passwordField, "4, 6, left, default");
		
		lblMMSection = new JLabel("Meet Details:");
		lblMMSection.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 15));
		contentPane.add(lblMMSection, "2, 10, 3, 1");
		
		lblFileChooser = new JLabel("Meet Data File:");
		contentPane.add(lblFileChooser, "2, 12, right, default");
		
		txtDataFile = new JTextField();
		contentPane.add(txtDataFile, "4, 12, left, default");
		txtDataFile.setColumns(30);
        txtDataFile.setText(filePath);
		
		btnChooseFile = new JButton("Browse\u2026");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(filePath);
				int returnVal = fc.showDialog(frmMain.this, "Select MM Database");
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            filePath = file.getAbsolutePath();
		            txtDataFile.setText(filePath);
		            //This is where a real application would open the file.
		            log.log(Level.FINE, "Opening: " + file.getName() + ".");
		        } else {
		            log.log(Level.FINE, "Open command cancelled by user.");
		        }
			}
		});
		contentPane.add(btnChooseFile, "6, 12, left, default");
		
		lblUploadInterval = new JLabel("Upload Interval (sec):");
		contentPane.add(lblUploadInterval, "2, 14");
		
		spnInterval = new JSpinner();
        Long seconds = new Long(preferences.get("seconds", valueOf(defaultSeconds)));
        final SpinnerNumberModel spnIntervalModel = new SpinnerNumberModel(seconds.intValue(), 10, 300, 10);
        interval = spnIntervalModel.getNumber().longValue() * 1000L;
        spnIntervalModel.setValue(seconds.intValue());
		spnInterval.setModel(spnIntervalModel);
		contentPane.add(spnInterval, "4, 14, left, default");
		spnInterval.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	interval = spnIntervalModel.getNumber().longValue() * 1000L;
	            log.info("interval changed: " + interval);
	        }
	    });
		
		lblMeetName = new JLabel("Meet Name:");
		contentPane.add(lblMeetName, "2, 16, right, default");

		cmbMeetName = new JComboBox(meets.getList());
		contentPane.add(cmbMeetName, "4, 16, fill, default");
		
		lblStatus = new JLabel("Status:");
		lblStatus.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 15));
		contentPane.add(lblStatus, "2, 18");
		
		lblLastUpload = new JLabel("Last Upload:");
		contentPane.add(lblLastUpload, "2, 20, right, default");
		
		txtLastUpload = new JTextField();
		contentPane.add(txtLastUpload, "4, 20, left, default");
		txtLastUpload.setColumns(20);

        lblUploadProgress = new JLabel("Upload Status:");
        contentPane.add(lblUploadProgress, "2, 22, right, default");
		
		progressBar = new JProgressBar();
		contentPane.add(progressBar, "4, 22");
		
		btnStartStop = new JButton("Start");
		contentPane.add(btnStartStop, "2, 24");
		btnStartStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                if (!uploaderStatus) {
                    log.info("Attempting to start uploader.");
                    start();
					uploaderStatus = true;
                    btnStartStop.setText("Stop");

                    // Disable things that can't be updated during uploader operation
                    txtUsername.setEnabled(false);
                    passwordField.setEnabled(false);
                    txtDataFile.setEnabled(false);
                    btnChooseFile.setEnabled(false);
                    cmbMeetName.setEnabled(false);
                } else {
                    log.info("Attempting to stop uploader");
                    uploaderStatus = false;
                    btnStartStop.setEnabled(false);
                    btnStartStop.setText("Start");
                    worker.cancel(true);
                    progressBar.setValue(0);
                    progressBar.setString("");
                    progressBar.setStringPainted(false);

                    // Enable things that can't be updated during uploader operation
                    txtUsername.setEnabled(true);
                    passwordField.setEnabled(true);
                    txtDataFile.setEnabled(true);
                    btnChooseFile.setEnabled(true);
                    cmbMeetName.setEnabled(true);
                }
			}
		});

		btnUploadNow = new JButton("Upload Now");
		contentPane.add(btnUploadNow, "4, 24, center, default");
        btnUploadNow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.info("Once of instant upload.");
                uploadNow();
            }
        });

        btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int exitConf = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Warning", JOptionPane.YES_NO_OPTION);

				if (exitConf == JOptionPane.YES_OPTION) {
                    preferences.put("username", txtUsername.getText());
                    preferences.put("seconds", new String().valueOf(spnIntervalModel.getValue()));
                    preferences.put("filePath", filePath);
                    System.exit(0);
                }

				
			}
		});
		contentPane.add(btnExit, "6, 24");

        // Resize window to accomodate everything
        this.pack();
        this.revalidate();

//        splash.close();
//        setVisible(true);
//        toFront();
	}
	
	/**
	 * Sets the last uploaded time to now
	 */
	public void setLastUpload() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		txtLastUpload.setText(dateFormat.format(cal.getTime())); 
		
	}

    /**
     * Updates progress bar to show message from web service
     *
     * @param status status message retrieved from eProgram upload web service
     */
    public void setLastStatus(String status) {
        progressBar.setStringPainted(true);
        progressBar.setString(status);
    }

    /**
     * Thread worker for automatic interval uploade
     */
    public void start() {
		
		worker = new SwingWorker<Boolean, Integer>() {
			
			@Override
			protected Boolean doInBackground() throws Exception {
				
				// Start the uploader
                uploader = new Uploader(filePath,
                        txtUsername.getText(),
                        passwordField.getText(),
                        meets.findMeetByName(valueOf(cmbMeetName.getSelectedItem())));
                uploader.setProgressBar(progressBar);

				while (isCancelled() == false) {
					
					// Attempt to upload and set status if successful
					if (uploader.upload()) {
						setLastUpload();
                        setLastStatus(uploader.getStatus());
                        //log.info("uploaded.");
                    }
					
					try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        btnStartStop.setEnabled(true);
                        return true;
                    }

                    log.fine("slept: " + interval);

                }

                log.fine("exited uploader loop");
                btnStartStop.setEnabled(true);
				return true;
			}
			
			protected void done() {
				boolean status;
				try {
					status = get();
                    log.fine("exited thread: " + status);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
				} catch (CancellationException e) {
                    log.info("Uploader stopped.");
                }
			}
			
			protected void process(List<Integer> chunks) {
				int mostRecentValue = chunks.get(chunks.size()-1);
			}
			
		};
		
		worker.execute();
		
	}

    /**
     * Thread worker for instant upload
     */
    public void uploadNow() {

        workerInstant = new SwingWorker<Boolean, Integer>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                // Start the uploader
                uploader = new Uploader(filePath,
                        txtUsername.getText(),
                        passwordField.getText(),
                        meets.findMeetByName(valueOf(cmbMeetName.getSelectedItem())));
                uploader.setProgressBar(progressBar);

                // Attempt to upload and set status if successful
                if (uploader.upload()) {
                    setLastUpload();
                    setLastStatus(uploader.getStatus());
                    //log.info("uploaded.");
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    btnStartStop.setEnabled(true);
                    return true;
                }

                return true;
            }

            protected void done() {
                boolean status;
                try {
                    status = get();
                    log.fine("exited thread: " + status);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
            }

            protected void process(List<Integer> chunks) {
                int mostRecentValue = chunks.get(chunks.size() - 1);
            }

        };

        workerInstant.execute();

    }

}
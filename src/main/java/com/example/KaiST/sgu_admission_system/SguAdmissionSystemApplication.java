package com.example.KaiST.sgu_admission_system;

import com.example.KaiST.sgu_admission_system.gui.GUI;
import javax.swing.SwingUtilities;

public class SguAdmissionSystemApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		SwingUtilities.invokeLater(() -> {
			GUI gui = new GUI();
			gui.setVisible(true);
		});
	}

}

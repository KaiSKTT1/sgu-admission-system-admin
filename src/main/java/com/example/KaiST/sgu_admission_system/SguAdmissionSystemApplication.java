package com.example.KaiST.sgu_admission_system;

import com.example.KaiST.sgu_admission_system.gui.GUI;
import javax.swing.SwingUtilities;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SguAdmissionSystemApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		new SpringApplicationBuilder(SguAdmissionSystemApplication.class)
				.headless(false)
				.run(args);
		SwingUtilities.invokeLater(() -> {
			GUI gui = new GUI();
			gui.setVisible(true);
		});
	}

}

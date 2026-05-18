package com.example.KaiST.sgu_admission_system;

import com.example.KaiST.sgu_admission_system.bus.XtUserBus;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import com.example.KaiST.sgu_admission_system.gui.GUI;
import com.example.KaiST.sgu_admission_system.gui.dialogs.LoginDialog;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import javax.swing.SwingUtilities;

public class SguAdmissionSystemApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		SwingUtilities.invokeLater(() -> {
			UiTheme.apply();
			XtUserBus userBus = new XtUserBus();
			LoginDialog loginDialog = new LoginDialog(null, userBus);
			loginDialog.setVisible(true);
			XtUser loggedIn = loginDialog.getLoggedIn();
			if (loggedIn == null) {
				System.exit(0);
				return;
			}
			GUI gui = new GUI(loggedIn);
			gui.setVisible(true);
		});
	}

}

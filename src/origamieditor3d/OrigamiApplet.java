// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
// Origami Editor 3D is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http:// www.gnu.org/licenses/>.
package origamieditor3d;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiApplet extends java.applet.Applet {
    
    @Override
    public void init() {
        
        final java.awt.Button launch = new java.awt.Button("Launch Origami Editor 3D now!");
        launch.setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
        launch.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                try {
                    new OrigamiEditorUI().setVisible(true);
                }
                catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });
        add(launch);
    }
}

// This file is part of Origami Editor 3D.
// Copyright (C) 2013 Bágyoni Attila <bagyoni.attila@gmail.com>
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

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;

/**
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 */
public interface BasicEditing {

    public boolean isTracked();
    public void update(Origami origami);
    public void setTracker(Camera refkamera, int x, int y);
    public void tiltLinerTo(Camera refkamera, Integer... xy);
    public void grabLinerAt(int vertIndex);
    public void reset();
}

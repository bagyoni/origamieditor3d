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
package origamieditor3d.origami;

/**
 * A PDF-exportnál használt alapértelmezett angol nyelvű szövegek.
 *
 * @author Attila Bágyoni <bagyoni.attila@gmail.com>
 */
public class Cookbook {

    static public String PDF_TITLE = " Folding Instructions)";
    static public String PDF_DISCLAIMER = "(Disclaimer:) ' "
            + "(This is an auto-generated document made by Origami Editor 3D,) ' "
            + "(a free, open-source WYSIWYG editor software dedicated to entirely) ' "
            + "(model paper folding.) '"
            + "(You are free to use, modify and redistribute this document.) ' "
            + "(Download Origami Editor 3D at <origamieditor3d.sourceforge.net>.) '";
    static public String PDF_STEPS = "(Number of steps: ";
    static public String PDF_PAPERTYPE = "(Paper type: ";
    static public String PDF_INTRO_A4 = "Start with an A4 piece of paper.) ' ";
    static public String PDF_INTRO_SQUARE = "Start with a square piece of paper.) ' ";
    static public String PDF_INTRO_HEX = "Start with a hexagonal piece of paper.) ' ";
    static public String PDF_INTRO_DOLLAR = "Start with a one-dollar bill.) ' ";
    static public String PDF_INTRO_TRIANGLE = "Start with this triangular) ' (piece of paper.) '";
    static public String PDF_INTRO_QUAD = "Start with this quadrilateral) ' (piece of paper.) '";
    static public String PDF_INTRO_POLYGON = "Start with this polygonal) ' (piece of paper.) '";
    static public String PDF_REFLECT_SOUTH = "Crease along the dashed line, then) ' "
            + "(fold everything below it inside-out.) ' ";
    static public String PDF_REFLECT_NORTH = "Crease along the dashed line, then) ' "
            + "(fold everything over it inside-out.) ' ";
    static public String PDF_REFLECT_EAST = "Crease along the dashed line, then) ' "
            + "(fold everything on its right side) ' "
            + "(inside-out.) ' ";
    static public String PDF_REFLECT_WEST = "Crease along the dashed line, then) ' "
            + "(fold everything on its left side) ' "
            + "(inside-out.) ' ";
    static public String PDF_ROTATE_SOUTH = "Turn everything below the) ' (dashed line by ";
    static public String PDF_ROTATE_NORTH = "Turn everything over the) ' (dashed line by ";
    static public String PDF_ROTATE_EAST = "Turn everything on the right side) ' "
            + "(of the dashed line by ";
    static public String PDF_ROTATE_WEST = "Turn everything on the left side) ' "
            + "(of the dashed line by ";
    static public String PDF_ROTATE_ANGLE = " degrees.) '";
    static public String PDF_REFLECT_TARGET = "Fold the entire gray part) ' "
            + "(inside-out \\(mountain folds become) ' "
            + "(valley folds and vice versa\\).) ' ";
    static public String PDF_ROTATE_TARGET = "Turn the gray flap around) ' "
            + "(the dashed line by ";
    static public String PDF_TURN = "Turn the whole figure around the) ' "
            + "(vertical axis by ";
    static public String PDF_TURN_ANGLE = " degrees) ' (\\(See the next picture\\).) '";
    static public String PDF_CREASE = "Crease along the dashed line to) ' "
            + "(create the gray part as seen in) ' "
            + "(Step ";
    static public String PDF_CREASE_STEP = ".) ' ";
    static public String PDF_OUTRO = "It's finished.) ' ";
}
JBeam 4.1 - Plane Frame and Truss Calculation for the Java Platform
===================================================================

   Copyright (C) 1998 Kai Gerd Schwebke

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


Installation
============

The program can be started right after decomprossion of the archive---
installation is not necessary.

To run JBeam a Java 11 VM is required (JDK or JRE version 11 or higher).

File Formats
============

JBeam 4.1 supports multiple file formats:
- **JSON format (.json)** - Human-readable, version control friendly (default)
- **JBeam native format (.jbm)** - Java serialization for backward compatibility

Both formats support the complete JBeam feature set including nodes, beams, 
constraints, loads, distributed loads, and internal hinges.

The standard language of the user interface after extraction
is english. The language is given from the entries in the
localisation file 'resourcen/locale.txt'.
This file can be replaced with an other language version.
A german localisation file is in the distribution ('resoucen/deutsch.txt'),
others can be created by the user if needed.


Lauching the Application
========================

The program is started from the installation directory by
calling the Java-VM with the option '-jar jbeam.jar'.
Some starting scripts for Unix- and Windows-systems are distributed.

Simply double click the file 'jbeam.jar' under Windows, alternativly.


Feedback
========

Feel free to submit comments, suggestions or problems to
jbeam@schwebke.com.

----------------------------------------------------------------------------
http://jbeam.schwebke.com/

/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package csplugins.id.mapping.ui;

import org.bridgedb.DataSource;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author gjj
 */
class DataSourceWrapper implements Comparable {
        private DataSource ds;
        static private Map<DataSource, DataSourceWrapper> wrappers
                = new HashMap();

        static DataSourceWrapper getInstance(DataSource ds) {
            if (ds==null) {
                return null;
            }

            DataSourceWrapper wrapper = wrappers.get(ds);
            if (wrapper==null) {
                wrapper = new DataSourceWrapper(ds);
                wrappers.put(ds, wrapper);
            }

            return wrapper;
        }

        private DataSourceWrapper(DataSource ds) {
            this.ds = ds;
        }

        DataSource DataSource() {
            return ds;
        }

        @Override
        public String toString() {
            String fullName = ds.getFullName();
            if (fullName!=null)
                return fullName;

            String sysCode = ds.getSystemCode();
            if (sysCode!=null) {
                return sysCode;
            } 

            return null;
        }

        public int compareTo(Object obj) {
            return this.toString().compareTo(obj.toString());
        }
    }

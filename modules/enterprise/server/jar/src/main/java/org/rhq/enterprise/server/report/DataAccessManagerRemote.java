/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.enterprise.server.report;

import java.util.List;

import javax.ejb.Remote;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.util.PageControl;

/**
 * @author Greg Hinkle
 */
@Remote
public interface DataAccessManagerRemote {

    /**
     * Execute a query. Requires a user with the MANAGE_INVENTORY permission.
     *
     * @param subject an admin user's subject
     * @param query the query to execute
     * @return a list of object results. Each entry in the rows array will represent an item from the select clause
     */
    public List<Object[]> executeQuery(//
        Subject subject, String query);

    /**
     * Execute a query filtered by a page control
     * 
     * @param subject an admin user's subject
     * @param query the query to execute
     * @param pageControl pages to load
     * @return list of object array results
     */
    public List<Object[]> executeQueryWithPageControl(//
        Subject subject, String query, PageControl pageControl);

}

/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
/**
 *
 */
package it.geosolutions.iengine.ui.mvc;

import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.flow.file.FileBasedFlowManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Alessio
 * 
 */
public class FlowManagerResumeController extends AbstractController {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Catalog catalog = (Catalog) getApplicationContext().getBean("catalog");

        String fmId = request.getParameter("fmId");

        if (fmId != null) {
            FileBasedFlowManager fm = catalog.getResource(fmId, FileBasedFlowManager.class);

            if ((fm != null) && !fm.isRunning()) {
                fm.resume();
            }
        }

        ModelAndView mav = new ModelAndView("index");
        mav.addObject("flowManagers", catalog.getFlowManagers(FileBasedFlowManager.class));

        return mav;
    }
}

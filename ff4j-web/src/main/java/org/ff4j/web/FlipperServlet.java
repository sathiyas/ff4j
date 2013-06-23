package org.ff4j.web;

import static org.ff4j.web.FlipperServletGui.DESCRIPTION;
import static org.ff4j.web.FlipperServletGui.FEATID;
import static org.ff4j.web.FlipperServletGui.FLIPFILE;
import static org.ff4j.web.FlipperServletGui.OPERATION;
import static org.ff4j.web.FlipperServletGui.OP_ADD_FEATURE;
import static org.ff4j.web.FlipperServletGui.OP_ADD_ROLE;
import static org.ff4j.web.FlipperServletGui.OP_DISABLE;
import static org.ff4j.web.FlipperServletGui.OP_EDIT_FEATURE;
import static org.ff4j.web.FlipperServletGui.OP_ENABLE;
import static org.ff4j.web.FlipperServletGui.OP_EXPORT;
import static org.ff4j.web.FlipperServletGui.OP_RMV_FEATURE;
import static org.ff4j.web.FlipperServletGui.OP_RMV_ROLE;
import static org.ff4j.web.FlipperServletGui.ROLE;
import static org.ff4j.web.FlipperServletGui.renderButtonDeleteFeature;
import static org.ff4j.web.FlipperServletGui.renderButtonEditFeature;
import static org.ff4j.web.FlipperServletGui.renderButtonUserRole;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.ff4j.Feature;
import org.ff4j.Flipper;
import org.ff4j.store.FeatureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unique Servlet to manage FlipPoints and security
 *
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
public class FlipperServlet extends HttpServlet{

	/** serial number. */
	private static final long serialVersionUID = -3982043895954284269L;
	
	/** Logger for Advisor. */
	final static Logger LOG = LoggerFactory.getLogger(FlipperServlet.class);
	
	/** {@inheritDoc} */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		
		String message 			= null;
		String messagetype 		= "error";
		
		// Routing on pagename
		String operation = req.getParameter(OPERATION);
		
		// Idependant of page to display (not same ops)
		try {
			if (operation != null && !operation.isEmpty()) {
				if (OP_DISABLE.equalsIgnoreCase(operation)) {
					opDisableFeature(req);
					messagetype = "info";
					message = "FliPoint <b>" + req.getParameter(FEATID) + " </b> has been successfully DISABLED";
				} else if (OP_ENABLE.equalsIgnoreCase(operation)) {
					opEnableFeature(req);
					messagetype = "info";
					message = "FliPoint <b>" + req.getParameter(FEATID) + " </b> has been successfully ENABLED";
				} else if (OP_EDIT_FEATURE.equalsIgnoreCase(operation)) {
					opUpdateFeatureDescription(req);
					messagetype = "info";
					message = "FliPoint <b>" + req.getParameter(FEATID) + " </b> has been successfully updated";
				} else if (OP_ADD_FEATURE.equalsIgnoreCase(operation)) {
					opAddNewFeature(req);
					messagetype = "info";
					message = "FliPoint <b>" + req.getParameter(FEATID) + " </b> has been successfully added";
				} else if (OP_RMV_FEATURE.equalsIgnoreCase(operation)) {
					opDeleteFeature(req);
					messagetype = "info";
					message = "FliPoint <b>" + req.getParameter(FEATID) + " </b> has been successfully deleted";
				} else if (OP_ADD_ROLE.equalsIgnoreCase(operation)) {
					opAddRoleToFeature(req);
					messagetype = "info";
					message = "Role <b>" + req.getParameter(ROLE) + 
							"</b> has been successfully added to flipPoint <b>" + req.getParameter(FEATID) + " </b>";
				} else if (OP_RMV_ROLE.equalsIgnoreCase(operation)) {
					opRemoveRoleFromFeature(req);
					messagetype = "info";
					message = "Role <b>" + req.getParameter(ROLE) + 
							"</b> has been successfully removed from flipPoint <b>" + req.getParameter(FEATID) + " </b>";
				} else if (OP_EXPORT.equalsIgnoreCase(operation)) {
					InputStream in = FeatureLoader.exportFeatures(Flipper.getStore().readAll());
					ServletOutputStream sos = res.getOutputStream();
					res.setContentType("text/xml");
					res.setHeader("Content-Disposition", "attachment; filename=\"ff4j.xml\"");
					//res.setContentLength()
					byte[] bbuf = new byte[4096];
					int length = 0;
					while ((in != null) && ((length = in.read(bbuf))  != -1)) {
						sos.write(bbuf, 0, length);
					}
					in.close();
					sos.flush();
					sos.close();
				}
			}
		} catch (Exception e) {
			message = e.getMessage();
		}
		renderPage(req, res, message, messagetype);
	}
    
	/** {@inheritDoc} */
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String message 			= null;
		String messagetype 		= "error";
		try {
	        List < FileItem > items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (FileItem item : items) {
	            if (item.isFormField() ) {
	            	if (OPERATION.equalsIgnoreCase(item.getFieldName())) {
	            		//String operation = item.getString();
	            	}
	            } else if (FLIPFILE.equalsIgnoreCase(item.getFieldName())) {
	            		String filename = FilenameUtils.getName(item.getName());
		                if (filename.toLowerCase().endsWith("xml")) {
		                	opImportFile(item.getInputStream());
		                } else {
		                	messagetype = "error";
			    			message     = "Invalid FILE, must be CSV, XML or PROPERTIES files";
		                }
	            	}
	            }
	    } catch (Exception e) {
	    	message = e.getMessage();
	    }
		renderPage(req, res, message, messagetype);
	}

	/**
	 * Render the ff4f console webpage through different block.
	 *
	 * @param req
	 * 		http request (with parameters)
	 * @param res
	 * 		http response (with outouput test)
	 * @param message
	 * 		text in the information box (blue/green/orange/red)
	 * @param messagetype
	 * 		type of informatice message (info,success,warning,error)
	 * @throws IOException
	 * 		error during populating http response
	 */
	private void renderPage(HttpServletRequest req, HttpServletResponse res, String message, String messagetype)
	throws IOException {
		// Render PAGE
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println(FlipperServletGui.HEADER);
		out.println(FlipperServletGui.renderNavBar(req));
		out.println("<div class=\"container\">");
		out.print(renderSectionFeatures(req, message, messagetype));
		out.println(FlipperServletGui.renderModalEditFlip(req));
		out.println(FlipperServletGui.renderModalNewFlipPoint(req));
		out.println(FlipperServletGui.renderModalImportFlipPoints(req));
		out.println("</body>");
		out.println("</html>");
	}

	/**
	 * User action to enable a Feature.
	 * 
	 * @param req
	 * 		http request containing operation parameters
	 */
    private void opEnableFeature(HttpServletRequest req) {
    	final String featureId = req.getParameter(FEATID);
    	if (featureId != null && !featureId.isEmpty()) {
			Flipper.enableFeature(featureId);
    	}
    }
    
    /**
	 * User action to disable a Feature.
	 * 
	 * @param req
	 * 		http request containing operation parameters
	 */
    private void opDisableFeature(HttpServletRequest req) {
    	final String featureId = req.getParameter(FEATID);
    	if (featureId != null && !featureId.isEmpty()) {
    		Flipper.disableFeature(featureId);
    	}
    }
    
    /**
	 * User action to create a new Feature.
	 * 
	 * @param req
	 * 		http request containing operation parameters
	 */
    private void opAddNewFeature(HttpServletRequest req) {
    	final String featureId   = req.getParameter(FEATID);
    	final String featureDesc = req.getParameter(DESCRIPTION); 
    	if (featureId != null && !featureId.isEmpty()) {
    		Feature fp = new Feature(featureId, false, featureDesc);
			Flipper.getStore().create(fp);
    	}
    }
    
    /**
	 * User action to delete a new Feature.
	 * 
	 * @param req
	 * 		http request containing operation parameters
	 */
    private void opDeleteFeature(HttpServletRequest req) {
    	final String featureId   = req.getParameter(FEATID);
    	if (featureId != null && !featureId.isEmpty()) {
			Flipper.getStore().delete(featureId);
    	}
    }
    
    /**
	 * User action to update a target feature's description.
	 * 
	 * @param req
	 * 		http request containing operation parameters
	 */
    private void opUpdateFeatureDescription(HttpServletRequest req) {
    	final String featureId   = req.getParameter(FEATID);
    	final String description = req.getParameter(DESCRIPTION);
    	if (featureId != null && !featureId.isEmpty()) {
    		Feature fp = Flipper.getStore().read(featureId);
    		fp.setDescription(description);
			Flipper.getStore().update(fp);
    	}
    }
    
    /**
   	 * User action to add a role to feature.
   	 * 
   	 * @param req
   	 * 		http request containing operation parameters
   	 */
    private void opAddRoleToFeature(HttpServletRequest req) {
    	final String flipId   = req.getParameter(FEATID);
    	final String roleName = req.getParameter(ROLE);
    	Flipper.getStore().grantRoleOnFeature(flipId, roleName);
    }
    
    /**
   	 * User action to remove a role from feature.
   	 * 
   	 * @param req
   	 * 		http request containing operation parameters
   	 */
    private void opRemoveRoleFromFeature(HttpServletRequest req) {
    	final String flipId   = req.getParameter(FEATID);
    	final String roleName = req.getParameter(ROLE);
    	Flipper.getStore().removeRoleFromFeature(flipId, roleName);
    }
    
    /**
     * User action to import Features from a properties files.
     *
     * @param in
     * 		inpustream from configuration file
     * @throws IOException
     * 		Error raised if the configuration cannot be read
     */
	private void opImportFile(InputStream in) throws IOException {
		LinkedHashMap<String, Feature> mapsOfFeat = FeatureLoader.loadFeatures(in);
		for (String featureName : mapsOfFeat.keySet()) {
			LOG.info("Processing FlipPoint " + featureName);
			if (Flipper.getStore().exist(featureName)) {
				Flipper.getStore().update(mapsOfFeat.get(featureName));
			} else {
				Flipper.getStore().create(mapsOfFeat.get(featureName));
			}
		}
	}
	
    /**
     * Produce HTML output to display the feature table.
     *
     * @param req
     * 		http request containing parameters
     * @return
     * 		HTML text output
     */
    private String renderSectionFeatures(HttpServletRequest req, String message, String type) {
    	Map <String, Feature> mapOfFlipPoints = new LinkedHashMap<String, Feature>();
    	if (Flipper.getFeatures() != null && !Flipper.getFeatures().isEmpty()) {
    		mapOfFlipPoints.putAll(Flipper.getFeatures());
    	}
        StringBuilder strB = new StringBuilder(FlipperServletGui.renderButtonsMainGroup(req));
        if (message != null && !message.isEmpty()) {
        	strB.append(FlipperServletGui.renderMessageBox(message, type));
 		}
        //strB.append(renderButtonImportFeatures(req));
        strB.append(FlipperServletGui.TABLE_HEADER);
        for (Feature fp : mapOfFlipPoints.values()) {
        	strB.append("<tr>");
        	strB.append("<td style=\"width:150px;font-weight:bold\">" + fp.getUid() + "</td>");
        	strB.append("<td style=\"width:300px;\">" + fp.getDescription() + "</td>");
        	strB.append("<td>");
        	Map <String, String> mapP = new LinkedHashMap<String, String>();
    		mapP.put("uid", fp.getUid());
        	if (fp.isEnabled()) {
        		strB.append(FlipperServletGui.renderElementButton(req, "Enabled", "success", "disable", mapP, null));
        	} else {
        		strB.append(FlipperServletGui.renderElementButton(req, "Disabled", "danger", "enable", mapP, null));
        	}
        	strB.append("</td>");
        	strB.append("<td style=\"width:20px;\">" + renderButtonEditFeature(req, fp.getUid())  + "</td>");
        	strB.append("<td style=\"width:20px;\">" + renderButtonDeleteFeature(req, fp.getUid()) + "</td>");
        	strB.append("<td style=\"width:85px;\">");
        	if (Flipper.getAuthorizationsManager() != null) {
        		strB.append(renderButtonUserRole(req, fp));
        	} else {
        		strB.append("no security policy");
        	}
        	strB.append("</td>");
        	strB.append("</tr>");
		}
        strB.append(MessageFormat.format(
        		FlipperServletGui.TABLE_FEATURES_FOOTER, req.getContextPath() + req.getServletPath()));
        
        return strB.toString();
    }
    
}

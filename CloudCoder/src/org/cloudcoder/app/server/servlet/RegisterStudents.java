// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaimespacco
 *
 */
public class RegisterStudents extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger=LoggerFactory.getLogger(RegisterStudents.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        Connection conn=null;
        try {
            conn=DBUtil.getConnection();
            
            //Create a temporary file in a platform-independent manner
            File tempFile = File.createTempFile("registerstudentsupload", ".tmp");
            tempFile.delete();
            tempFile.mkdirs();
            tempFile.deleteOnExit();

            DiskFileItemFactory factory=new DiskFileItemFactory(10*1024*1024, tempFile);
            ServletFileUpload upload = new ServletFileUpload(factory);

            int courseId=-1;
            InputStream in=null;
            int num=0;
            
            List<FileItem> items=upload.parseRequest(request);
            logger.info("num file items: "+items.size());
            for (FileItem item : items) {
                if (item.isFormField() && item.getFieldName().equals("courseId")) {
                    courseId=Integer.parseInt(item.getString());
                } else {
                    in=item.getInputStream();
                }
            }
            if (courseId==-1 || in==null) {
                logger.error("courseId "+courseId+", file input stream is null? "+(in==null));
                throw new ServletException("courseId "+courseId);
            }
            num=ConfigurationUtil.registerStudentsForCourseId(in,
                    courseId, 
                    conn);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            PrintStream out=new PrintStream(response.getOutputStream());
            out.println("Registered "+num+" students");
            out.flush();
            out.close();
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }
}

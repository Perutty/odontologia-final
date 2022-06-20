package co.empresa.odontologia.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import co.empresa.odontologia.dao.PacienteDao;
import co.empresa.odontologia.dao.PacienteDaoFactory;
import co.empresa.odontologia.modelo.Paciente;
/**
 * Servlet implementation class PacienteServlet
 */
@WebServlet(name="PacienteServlet", urlPatterns={"/PacienteServlet"})
@MultipartConfig(
		  fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		  maxFileSize = 1024 * 1024 * 10,      // 10 MB
		  maxRequestSize = 1024 * 1024 * 100   // 100 MB
		)
public class PacienteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final int CHUNK_SIZE=1024*4;
   
	private PacienteDao pacienteDao;
	
    public PacienteServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    public void init(ServletConfig config) throws ServletException {
		this.pacienteDao = PacienteDaoFactory.getPacienteDao("postgresql");
	}
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException, IOException {
    	
    	doGet(request, response);
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String action = request.getParameter("action");
		if(action == null) {
			action = "list";
		}
		
		try {
			switch(action) {
			case "newPaciente":
				showNewForm(request, response);
				break;
			case "editPaciente":
				showEditForm(request, response);
				break;
			case "insert":
				insertarPaciente(request,response);
				break;
			case "list":
				listPacientes(request,response);
				break;
			case "delete":
				deletePaciente(request,response);
				break;
			case "update":
				updatePaciente(request,response);
				break;
			case "cerrar":
				cerrarSesion(request,response);
				break;
			case "firmar":
				firmaPaciente(request,response);
				break;
			case "sp":
				sesionPaciente(request,response);
				break;
			default:
				listPacientes(request, response);
		}
	}catch(SQLException e) {
			throw new ServletException(e);
   }
}
	
	private void showNewForm(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("registrarPaci.jsp");
		dispatcher.forward(request, response);
	}
	
	private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
		
		int id = Integer.parseInt(request.getParameter("id"));
		Paciente pacienteActual = pacienteDao.select(id);
		request.setAttribute("paciente", pacienteActual);
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("registrarPaci.jsp");
		dispatcher.forward(request, response);
	}
	
	private void insertarPaciente(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
	    int doc_id = (int) request.getSession().getAttribute("doc_id");
	    Part filePart = request.getPart("archivo");
	    String fileName = filePart.getSubmittedFileName();
	    for (Part part : request.getParts()) {
	    part.write("c:/home/centos/fotos/" + fileName);}
	    
	    String cedula = request.getParameter("cedula");
	    String username = request.getParameter("username");
		String email = request.getParameter("email");
		String telefono = request.getParameter("telefono");
		Paciente paciente = new Paciente(cedula, username, email, telefono, doc_id);
		pacienteDao.insert(paciente);
		System.out.println(paciente);
		response.sendRedirect("PacienteServlet?action=list");
	}
	
	private void deletePaciente(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
		int id = Integer.parseInt(request.getParameter("id"));
		
		pacienteDao.delete(id);
		request.getRequestDispatcher("PacienteServlet?action=list").forward(request,response);
	}
	
	private void updatePaciente(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
		int id = Integer.parseInt(request.getParameter("id"));
		System.out.println(request.getParameter("id"));
		String cedula = request.getParameter("cedula");
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String telefono = request.getParameter("telefono");
		
		Paciente paciente = new Paciente(id,cedula,username, email, telefono);
		pacienteDao.update(paciente);
		System.out.println("Estoy donde jorge dijo");
		response.sendRedirect("PacienteServlet?action=list");
	}
	
	private void sesionPaciente(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
		int id = Integer.parseInt(request.getParameter("id"));
		request.getSession().setAttribute("paci_id", id);
		request.getRequestDispatcher("ProcesoServlet?action=list").forward(request,response);
	}
	
	private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
		request.getSession().invalidate();
		response.sendRedirect("OdontologoServlet");
	}
	
	private void firmaPaciente(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException {
		
		request.getRequestDispatcher("firma.jsp").forward(request, response);
	}
	
	private void listPacientes(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, SQLException, IOException{
		
		List<Paciente> listPacientes = pacienteDao.selectAll();
		List<Paciente> listMostrar = new ArrayList<>();
		if(request.getSession().getAttribute("doc_id") == null) {
			response.sendRedirect("OdontologoServlet");
		}
		else {
		int doc_id = (int) request.getSession().getAttribute("doc_id");
		
		listPacientes.forEach((pacientes) -> {
			if(pacientes.getDoc_id() == doc_id) {
				listMostrar.add(pacientes);
			}
		});
		request.getSession().setAttribute("listPacientes", listMostrar);
		request.getRequestDispatcher("dashboardDoc.jsp").forward(request, response);
		
	}
  }
}

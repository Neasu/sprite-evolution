package io;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import core.Program;

public class XMLReport
{
	// Variables
	private ArrayList<XMLReportEntry> 			entries			= null;
	private File 								destination 	= null;
	private Document							doc				= null;
	private Element								root			= null;
	
	// Constructors
	public XMLReport(File destination)
	{
		super();
		this.destination = destination;
		
		entries = new ArrayList<XMLReportEntry>();
		
		DocumentBuilderFactory	docFactory 	= DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder 		docBuilder	= docFactory.newDocumentBuilder();
			
			doc 	= docBuilder.newDocument();
			root 	= doc.createElement("report");
			doc.appendChild(root);
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	// Methods
	public void addEntry(XMLReportEntry entry)
	{
		if (entry != null)
		{
			entries.add(entry);
		}
	}
	
	public void generateReport()
	{
		for (XMLReportEntry xmlReportEntry : entries)
		{
			root.appendChild(xmlReportEntry.generateXMLElement(doc));
		}
		
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(destination);
			transformer.transform(source, result);
			
			Program.LOGGER.info("Report successfull generated and saved to " + destination.getAbsolutePath());
		}
		catch (Exception e)
		{
			Program.LOGGER.warning("Report generationfailed due to: " + e.getMessage());
		}
	}
}

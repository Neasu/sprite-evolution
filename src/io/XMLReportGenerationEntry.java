package io;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLReportGenerationEntry implements XMLReportEntry
{
	// Variables
	private int generationNumber 		= 0;
	private int generationSize			= 0;
	private int bestIndividualFileSize	= 0;
	private int fileSizeDecrease		= 0;
	
	// Constructors
	public XMLReportGenerationEntry(int generationNumber, int generationSize, int bestIndividualFileSize, int fileSizeDecrease)
	{
		super();
		this.generationNumber = generationNumber;
		this.generationSize = generationSize;
		this.bestIndividualFileSize = bestIndividualFileSize;
		this.fileSizeDecrease = fileSizeDecrease;
	}
	
	// Methods
	
	@Override
	public Element generateXMLElement(Document doc)
	{
		Element result = doc.createElement("generation");
		Attr attr = doc.createAttribute("number");
		attr.setValue("" + generationNumber);
		result.setAttributeNode(attr);
		
		Element generationSize = doc.createElement("generationSize");
		generationSize.appendChild(doc.createTextNode("" + this.generationSize));
		result.appendChild(generationSize);
		
		Element bestIndividualFileSize = doc.createElement("bestIndividualFileSize");
		bestIndividualFileSize.appendChild(doc.createTextNode("" + this.bestIndividualFileSize));
		result.appendChild(bestIndividualFileSize);
		
		Element fileSizeDecrease = doc.createElement("filesizeDecrease");
		fileSizeDecrease.appendChild(doc.createTextNode("" + this.fileSizeDecrease));
		result.appendChild(fileSizeDecrease);
		
		return result;
	}

	public int getGenerationNumber()
	{
		return generationNumber;
	}

	public int getGenerationSize()
	{
		return generationSize;
	}

	public int getBestIndividualFileSize()
	{
		return bestIndividualFileSize;
	}

	public int getFileSizeDecrease()
	{
		return fileSizeDecrease;
	}
}

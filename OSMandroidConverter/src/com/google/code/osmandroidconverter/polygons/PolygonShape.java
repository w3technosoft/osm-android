package com.google.code.osmandroidconverter.polygons;

import java.util.*;

public class PolygonShape {
	private Point[] m_aInputVertices;
	private Point[] m_aUpdatedPolygonVertices;
			
	private ArrayList m_alEars = new ArrayList();
	private  Point[][] m_aPolygons;

	public int NumberOfPolygons()
	{
		return m_aPolygons.length;	
	}
	
	public Point[] Polygons(int index)
	{
		if (index< m_aPolygons.length)
			return m_aPolygons[index];
		else
			return null;
	}
	
	public PolygonShape(Point[] vertices)
	{
		int nVertices=vertices.length;
		if (nVertices<3)
		{
			return;
		}

		//initalize the 2D points
		m_aInputVertices=new Point[nVertices];
 
		for (int i=0; i<nVertices; i++)
			m_aInputVertices[i] = vertices[i];
      
		//make a working copy,  m_aUpdatedPolygonVertices are
		//in count clock direction from user view
		SetUpdatedPolygonVertices();
	}
	
	/****************************************************
	To fill m_aUpdatedPolygonVertices array with input array.
	
	m_aUpdatedPolygonVertices is a working array that will 
	be updated when an ear is cut till m_aUpdatedPolygonVertices
	makes triangle (a convex polygon).
   ******************************************************/
	private void SetUpdatedPolygonVertices()
	{
		int nVertices=m_aInputVertices.length;
		m_aUpdatedPolygonVertices=new Point[nVertices];

		for (int i=0; i< nVertices; i++)
			m_aUpdatedPolygonVertices[i] =m_aInputVertices[i];
		
		//m_aUpdatedPolygonVertices should be in counter clock wise
		if (Polygon.PointsDirection(m_aUpdatedPolygonVertices)==PolygonDirection.Clockwise)
			Polygon.ReversePointsDirection(m_aUpdatedPolygonVertices);
	}
	
	public static int maxFromArray(int[] t) {
	    int maximum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];   // new maximum
	        }
	    }
	     return maximum;
	}
	
	public static int minFromArray(int[] t) {
	    int minimum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] < minimum) {
	            minimum = t[i];   // new maximum
	        }
	    }
	     return minimum;
	}
	    
	
	/**********************************************************
	To check the Pt is in the Triangle or not.
	If the Pt is in the line or is a vertex, then return true.
	If the Pt is out of the Triangle, then return false.

	This method is used for triangle only.
	***********************************************************/
	private boolean TriangleContainsPoint(Point[] trianglePts, Point pt)
	{
		if (trianglePts.length!=3)
			return false;
		
		
		
		//for (int i= trianglePts.GetLowerBound(0);i<trianglePts.GetUpperBound(0); i++)
		for (int i= 0;i<trianglePts.length; i++)
		{
			if (pt.EqualsPoint(trianglePts[i]))
				return true;
		}
		
		boolean bIn=false;

		LineSegment line0=new LineSegment(trianglePts[0],trianglePts[1]);
		LineSegment line1=new LineSegment(trianglePts[1],trianglePts[2]);
		LineSegment line2=new LineSegment(trianglePts[2],trianglePts[0]);

		if (pt.InLine(line0)||pt.InLine(line1)
			||pt.InLine(line2))
			bIn=true;
		else //point is not in the lines
		{
			double dblArea0=Polygon.PolygonArea(new Point[]
		{trianglePts[0],trianglePts[1], pt});
			double dblArea1=Polygon.PolygonArea(new Point[]
		{trianglePts[1],trianglePts[2], pt});
			double dblArea2=Polygon.PolygonArea(new Point[]
		{trianglePts[2],trianglePts[0], pt});

			if (dblArea0>0)
			{
				if ((dblArea1 >0) &&(dblArea2>0))
					bIn=true;
			}
			else if (dblArea0<0)
			{
				if ((dblArea1 < 0) && (dblArea2< 0))
					bIn=true;
			}
		}				
		return bIn;			
	}
	
	/****************************************************************
	To check whether the Vertex is an ear or not based updated Polygon vertices

	ref. www-cgrl.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian
	/algorithm1.html

	If it is an ear, return true,
	If it is not an ear, return false;
	*****************************************************************/
	private boolean IsEarOfUpdatedPolygon(Point vertex )		
	{
		Polygon polygon=new Polygon(m_aUpdatedPolygonVertices);

		if (polygon.PolygonVertex(vertex))
		{
			boolean bEar=true;
			if (polygon.PolygonVertexType(vertex)==VertexType.ConvexPoint)
			{
				Point pi=vertex;
				Point pj=polygon.PreviousPoint(vertex); //previous vertex
				Point pk=polygon.NextPoint(vertex);//next vertex

				//for (int i=m_aUpdatedPolygonVertices.GetLowerBound(0);i<m_aUpdatedPolygonVertices.GetUpperBound(0); i++)
				for (int i=0;i<m_aUpdatedPolygonVertices.length; i++)
				{
					Point pt = m_aUpdatedPolygonVertices[i];
					if ( !(pt.EqualsPoint(pi)|| pt.EqualsPoint(pj)||pt.EqualsPoint(pk)))
					{
						if (TriangleContainsPoint(new Point[] {pj, pi, pk}, pt))
							bEar=false;
					}
				}
			} //ThePolygon.getVertexType(Vertex)=ConvexPt
			else  //concave point
				bEar=false; //not an ear/
			return bEar;
		}
		else //not a polygon vertex;
		{
			
			return false;
		}
	}
	
	/****************************************************
	Set up m_aPolygons:
	add ears and been cut Polygon togather
	****************************************************/
	private void SetPolygons()
	{   
		int nPolygon=m_alEars.size() + 1; //ears plus updated polygon
		m_aPolygons=new Point[nPolygon][];

		for (int i=0; i<nPolygon-1; i++) //add ears
		{
			Point[] points=(Point[])m_alEars.get(i);

			m_aPolygons[i]=new Point[3]; //3 vertices each ear
			m_aPolygons[i][0]=points[0];
			m_aPolygons[i][1]=points[1];
			m_aPolygons[i][2]=points[2];
		}
			
		//add UpdatedPolygon:
		m_aPolygons[nPolygon-1]=new Point[m_aUpdatedPolygonVertices.length];

		for (int i=0; i<m_aUpdatedPolygonVertices.length;i++)
		{
			m_aPolygons[nPolygon-1][i] = m_aUpdatedPolygonVertices[i];
		}
	}

	/********************************************************
	To update m_aUpdatedPolygonVertices:
	Take out Vertex from m_aUpdatedPolygonVertices array, add 3 points
	to the m_aEars
	**********************************************************/
	private void UpdatePolygonVertices(Point vertex)
	{
		ArrayList alTempPts=new ArrayList(); 

		for (int i=0; i< m_aUpdatedPolygonVertices.length; i++)
		{				
			if (vertex.EqualsPoint(
				m_aUpdatedPolygonVertices[i])) //add 3 pts to FEars
			{ 
				Polygon polygon=new Polygon(m_aUpdatedPolygonVertices);
				Point pti = vertex;
				Point ptj = polygon.PreviousPoint(vertex); //previous point
				Point ptk = polygon.NextPoint(vertex); //next point
				
				Point[] aEar=new Point[3]; //3 vertices of each ear
				aEar[0]=ptj;
				aEar[1]=pti;
				aEar[2]=ptk;

				m_alEars.add(aEar);
			}
			else	
			{
				alTempPts.add(m_aUpdatedPolygonVertices[i]);
			} //not equal points
		}
		
		if  (m_aUpdatedPolygonVertices.length- alTempPts.size()==1)
		{
			int nLength=m_aUpdatedPolygonVertices.length;
			m_aUpdatedPolygonVertices=new Point[nLength-1];
    
			for (int  i=0; i<alTempPts.size(); i++)
				m_aUpdatedPolygonVertices[i]=(Point)alTempPts.get(i);
		}
	}	
	
	/*******************************************************
	To cut an ear from polygon to make ears and an updated polygon:
	*******************************************************/
	public void CutEar()
	{

		
		Polygon polygon=new Polygon(m_aUpdatedPolygonVertices);
		boolean bFinish=false;

		//if (polygon.GetPolygonType()==PolygonType.Convex) //don't have to cut ear
		//	bFinish=true;

		if (m_aUpdatedPolygonVertices.length==3) //triangle, don't have to cut ear
			bFinish=true;
		
		Point pt=new Point();
		while (bFinish==false) //UpdatedPolygon
		{
			
			int i=0;
			boolean bNotFound=true;
			while (bNotFound && (i<m_aUpdatedPolygonVertices.length)) //loop till find an ear
			{
				pt=m_aUpdatedPolygonVertices[i];
				if (IsEarOfUpdatedPolygon(pt))
					bNotFound=false; //got one, pt is an ear
				else
					i++;
			} //bNotFount
			//An ear found:}
			if (pt !=null)
				UpdatePolygonVertices(pt);
   
			polygon=new Polygon(m_aUpdatedPolygonVertices);
			//if ((polygon.GetPolygonType()==PolygonType.Convex)
			//	&& (m_aUpdatedPolygonVertices.Length==3))
			if (m_aUpdatedPolygonVertices.length==3)
				bFinish=true;
		} //bFinish=false
		SetPolygons();
	}		

	public double area() {
		return Math.abs(signedArea()); 
	}
	
    public double signedArea() {
    	Point[] points=m_aInputVertices;
        double sum = 0.0;
        for (int i = 0; i < m_aInputVertices.length-1; i++) {
            sum += (points[i].getX() * points[i+1].getY() - points[i+1].getX()*points[i].getY());
        }
        
        return 0.5 * sum;

    }
    
	public Point findCentroid(){

		if (Polygon.PointsDirection(m_aInputVertices) == PolygonDirection.Count_Clockwise){
			Polygon.ReversePointsDirection(m_aInputVertices);
		}

		  double cx = 0.0, cy = 0.0, f = 0;
		  Point[] p=m_aInputVertices;
	      for (int i = 0; i < m_aInputVertices.length-1; i++) {
	    	  
	    	  	f = p[i].getX() * p[i+1].getY() - p[i+1].getX() * p[i].getY(); 
	    	  	
	    	  	cx += (p[i].getX() + p[i+1].getX()) * f; 
	            cy += (p[i].getY() + p[i+1].getY()) * f;
	            
	      }
	        cx = 1/(6 * area()) * cx;
	        cy = 1/(6 * area()) * cy;
	        return new Point(cx, cy);
	      		
	}


}

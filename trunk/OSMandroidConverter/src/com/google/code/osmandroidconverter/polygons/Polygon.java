package com.google.code.osmandroidconverter.polygons;

public class Polygon {

	
	private static double smallValue = 0.00001;
	private static double bigValue   = 99999;
	
	private Point[] m_aVertices;

	public void setThisIndex(int index,Point value) 
	{	
			m_aVertices[index]=value;
	}
	
	public Point getThisIndex(int index) 
	{	
			return m_aVertices[index];
	}
		
	public Polygon()
	{
	
	}

	public Polygon(Point[] points)
	{
		int nNumOfPoitns=points.length;
		try
		{
			if (nNumOfPoitns<3 )
			{     
				throw new Exception();
			}
			else
			{
				m_aVertices=new Point[nNumOfPoitns];
				for (int i=0; i<nNumOfPoitns; i++)
				{
					m_aVertices[i]=points[i];
				}
			}
		}
		catch (Exception e)
		{
			;
		}
	}
	
	/***********************************
	 From a given point, get its vertex index.
	 If the given point is not a polygon vertex, 
	 it will return -1 
	 ***********************************/
	public int VertexIndex(Point vertex)
	{
		int nIndex=-1;

		int nNumPts=m_aVertices.length;
		for (int i=0; i<nNumPts; i++) //each vertex
		{
			if (Point.SamePoints(m_aVertices[i], vertex))
				nIndex=i;
		}
		return nIndex;
	}

	/***********************************
	 From a given vertex, get its previous vertex point.
	 If the given point is the first one, 
	 it will return  the last vertex;
	 If the given point is not a polygon vertex, 
	 it will return null; 
	 ***********************************/
	public Point PreviousPoint(Point vertex)
	{
		int nIndex;
		
		nIndex=VertexIndex(vertex);
		if (nIndex==-1)
			return null;
		else //a valid vertex
		{
			if (nIndex==0) //the first vertex
			{
				int nPoints=m_aVertices.length;
				return m_aVertices[nPoints-1];
			}
			else //not the first vertex
				return m_aVertices[nIndex-1];
		}			
	}

	/***************************************
	 From a given vertex, get its next vertex point.
	 If the given point is the last one, 
	 it will return  the first vertex;
	 If the given point is not a polygon vertex, 
	 it will return null; 
***************************************/
public Point NextPoint(Point vertex)
{
	Point nextPt=new Point();

	int nIndex;
	nIndex=VertexIndex(vertex);
	if (nIndex==-1)
		return null;
	else //a valid vertex
	{
		int nNumOfPt=m_aVertices.length;
		if (nIndex==nNumOfPt-1) //the last vertex
		{
			return m_aVertices[0];
		}
		else //not the last vertex
			return m_aVertices[nIndex+1];
	}			
}

/******************************************
To calculate the polygon's area

Good for polygon with holes, but the vertices make the 
hole  should be in different direction with bounding 
polygon.

Restriction: the polygon is not self intersecting
ref: www.swin.edu.au/astronomy/pbourke/
	geometry/polyarea/
*******************************************/
public double PolygonArea()
{
	double dblArea=0;
	int nNumOfVertices=m_aVertices.length;
	
	int j;
	for (int i=0; i<nNumOfVertices; i++)
	{
		j=(i+1) % nNumOfVertices;
		dblArea += m_aVertices[i].getX()*m_aVertices[j].getY();
		dblArea -= (m_aVertices[i].getY()*m_aVertices[j].getX());
	}

	dblArea=dblArea/2;
	return Math.abs(dblArea);
}

/******************************************
To calculate the area of polygon made by given points 

Good for polygon with holes, but the vertices make the 
hole  should be in different direction with bounding 
polygon.

Restriction: the polygon is not self intersecting
ref: www.swin.edu.au/astronomy/pbourke/
	geometry/polyarea/

As polygon in different direction, the result coulb be
in different sign:
If dblArea>0 : polygon in clock wise to the user 
If dblArea<0: polygon in count clock wise to the user 		
*******************************************/
public static double PolygonArea(Point[] points)
{
	double dblArea=0;
	int nNumOfPts=points.length;
	
	int j;
	for (int i=0; i<nNumOfPts; i++)
	{
		j=(i+1) % nNumOfPts;
		dblArea += points[i].getX()*points[j].getY();
		dblArea -= (points[i].getY()*points[j].getX());
	}

	dblArea=dblArea/2;
	return dblArea;
}



/***********************************************
To check a vertex concave point or a convex point
-----------------------------------------------------------
The out polygon is in count clock-wise direction
************************************************/
public int PolygonVertexType(Point vertex)
{
	int vertexType=VertexType.ErrorPoint;
	
	if (PolygonVertex(vertex))			
	{
		Point pti=vertex;
		Point ptj=PreviousPoint(vertex);
		Point ptk=NextPoint(vertex);		
	
		double dArea=PolygonArea(new Point[] {ptj,pti, ptk});
		
		if (dArea<0)
			vertexType= VertexType.ConvexPoint;
		else if (dArea> 0)
			vertexType= VertexType.ConcavePoint;
	}	
	return vertexType;
}

/*********************************************
To check the Line of vertex1, vertex2 is a Diagonal or not

To be a diagonal, Line vertex1-vertex2 has no intersection 
with polygon lines.

If it is a diagonal, return true;
If it is not a diagonal, return false;
reference: www.swin.edu.au/astronomy/pbourke
/geometry/lineline2d
*********************************************/
public boolean Diagonal(Point vertex1, Point vertex2)
{
	boolean bDiagonal=false;
	int nNumOfVertices=m_aVertices.length;
	int j=0;
	for (int i= 0; i<nNumOfVertices; i++) //each point
	{
		bDiagonal=true;
		j= (i+1) % nNumOfVertices;  //next point of i

		//Diagonal line:
		double x1=vertex1.getX();
		double y1=vertex1.getY();
		double x2=vertex1.getX();
		double y2=vertex1.getY();

		//CPolygon line:
		double x3=m_aVertices[i].getX();
		double y3=m_aVertices[i].getY();
		double x4=m_aVertices[j].getX();
		double y4=m_aVertices[j].getY();

		double de=(y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
		double ub=-1;
		
		if (Math.abs(de-0)>smallValue)  //lines are not parallel
			ub=((x2-x1)*(y1-y3)-(y2-y1)*(x1-x3))/de;

		if ((ub> 0) && (ub<1))
		{
			bDiagonal=false;
		}
	}
	return bDiagonal;
}

/*************************************************
To check FaVertices make a convex polygon or 
concave polygon

Restriction: the polygon is not self intersecting
Ref: www.swin.edu.au/astronomy/pbourke
/geometry/clockwise/index.html
********************************************/
public int GetPolygonType()
{
	int nNumOfVertices=m_aVertices.length;
	boolean bSignChanged=false;
	int nCount=0;
	int j=0, k=0;

	for (int i=0; i<nNumOfVertices; i++)
	{
		j=(i+1) % nNumOfVertices; //j:=i+1;
		k=(i+2) % nNumOfVertices; //k:=i+2;

		double crossProduct=(m_aVertices[j].getX()- m_aVertices[i].getX())
			*(m_aVertices[k].getY()- m_aVertices[j].getY());
		crossProduct=crossProduct-(
			(m_aVertices[j].getY()- m_aVertices[i].getY())
			*(m_aVertices[k].getX()- m_aVertices[j].getX())
			);

		//change the value of nCount
		if ((crossProduct>0) && (nCount==0) )
			nCount=1;
		else if ((crossProduct<0) && (nCount==0))
			nCount=-1;

		if (((nCount==1) && (crossProduct<0))
			||( (nCount==-1) && (crossProduct>0)) )
			bSignChanged=true;
	}

	if (bSignChanged)
		return PolygonType.Concave;
	else
		return PolygonType.Convex;
}

/***************************************************
Check a Vertex is a principal vertex or not
ref. www-cgrl.cs.mcgill.ca/~godfried/teaching/
cg-projects/97/Ian/glossay.html

PrincipalVertex: a vertex pi of polygon P is a principal vertex if the
diagonal pi-1, pi+1 intersects the boundary of P only at pi-1 and pi+1.
*********************************************************/
public boolean PrincipalVertex(Point vertex)
{
	boolean bPrincipal=false;
	if (PolygonVertex(vertex)) //valid vertex
	{
		Point pt1=PreviousPoint(vertex);
		Point pt2=NextPoint(vertex);
			
		if (Diagonal(pt1, pt2))
			bPrincipal=true;
	}
	return bPrincipal;
}

/*********************************************
To check whether a given point is a CPolygon Vertex
**********************************************/
public boolean PolygonVertex(Point point)
{
	boolean bVertex=false;
	int nIndex=VertexIndex(point);

	if ((nIndex>=0) && (nIndex<=m_aVertices.length-1))
					   bVertex=true;

	return bVertex;
}

/*****************************************************
To reverse polygon vertices to different direction:
clock-wise <------->count-clock-wise
******************************************************/
public void ReverseVerticesDirection()
{
	int nVertices=m_aVertices.length;
	Point[] aTempPts=new Point[nVertices];
	
	for (int i=0; i<nVertices; i++)
		aTempPts[i]=m_aVertices[i];

	for (int i=0; i<nVertices; i++)
	m_aVertices[i]=aTempPts[nVertices-1-i];	
}

/*****************************************
To check vertices make a clock-wise polygon or
count clockwise polygon

Restriction: the polygon is not self intersecting
Ref: www.swin.edu.au/astronomy/pbourke/
geometry/clockwise/index.html
*****************************************/
public int VerticesDirection()
{
	int nCount=0, j=0, k=0;
	int nVertices=m_aVertices.length;
	
	for (int i=0; i<nVertices; i++)
	{
		j=(i+1) % nVertices; //j:=i+1;
		k=(i+2) % nVertices; //k:=i+2;

		double crossProduct=(m_aVertices[j].getX() - m_aVertices[i].getX())
			*(m_aVertices[k].getY()- m_aVertices[j].getY());
		crossProduct=crossProduct-(
			(m_aVertices[j].getY()- m_aVertices[i].getY())
			*(m_aVertices[k].getX()- m_aVertices[j].getX())
			);

		if (crossProduct>0)
			nCount++;
		else
			nCount--;
	}

	if( nCount<0) 
		return PolygonDirection.Count_Clockwise;
	else if (nCount> 0)
		return PolygonDirection.Clockwise;
	else
		return PolygonDirection.Unknown;
	}


/*****************************************
To check given points make a clock-wise polygon or
count clockwise polygon

Restriction: the polygon is not self intersecting
*****************************************/
public static int PointsDirection(Point[] points)
{
	int nCount=0, j=0, k=0;
	int nPoints=points.length;
	
	if (nPoints<3)
		return PolygonDirection.Unknown;
	
	for (int i=0; i<nPoints; i++)
	{
		j=(i+1) % nPoints; //j:=i+1;
		k=(i+2) % nPoints; //k:=i+2;

		double crossProduct=(points[j].getX() - points[i].getX())*(points[k].getY()- points[j].getY());
		crossProduct=crossProduct-(	(points[j].getY()- points[i].getY())*(points[k].getX()- points[j].getX()));

		if (crossProduct>0)
			nCount++;
		else
			nCount--;
	}

	if( nCount<0) 
		return PolygonDirection.Count_Clockwise;
	else if (nCount> 0)
		return PolygonDirection.Clockwise;
	else
		return PolygonDirection.Unknown;
}

/*****************************************************
To reverse points to different direction (order) :
******************************************************/
public static void ReversePointsDirection(Point[] points)
{
	int nVertices=points.length;
	Point[] aTempPts=new Point[nVertices];
	
	for (int i=0; i<nVertices; i++)
		aTempPts[i]=points[i];

	for (int i=0; i<nVertices; i++)
		points[i]=aTempPts[nVertices-1-i];	
}

}

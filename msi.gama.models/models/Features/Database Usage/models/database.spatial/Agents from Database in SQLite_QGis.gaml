/**
 *  
 *  Author: Truong Minh Thai (thai.truongminh@gmail.com)
 *  Description: 
 *      This model loads data from vnm_adm2 that is created by QGis.
 * 	    In this case we do not need using AsBinary() to convert blob data to WKB format.  
 *      
 * 		In other case, if we load Geometry data that is created by using libspatialite library then we must use Asbinary()
 *      to convert geometry to WKB format (see SQLite_libspatialite model)
 * See: 
 *     SQLite_libspatialite model
 * 	
 */

model Sqlite_QGis
  
/* Insert your model definition here */
 
  
global { 
	map BOUNDS <- ['srid'::'4326',
				  "dbtype"::"sqlite","database"::"../includes/bph.db"
				  ,"select"::	"select geometry  from vnm_adm2 where (id_1=3291 or id_1=3289);" //Bac trung bo and DBSCL
														
							  ]; 
	map PARAMS <- ['srid'::'4326',"dbtype"::"sqlite","database"::"../includes/bph.db"];
	string LOCATIONS <- "select ID_2, varname_2,  geometry  from vnm_adm2 where id_1=3291 or id_1=3289;";
	init {
		create dummy 
		{ 			
			create locations from: list(self select [params:: PARAMS, select:: LOCATIONS]) with:[ id:: int("id_2"), name:: "varname_2",shape::geometry("geometry")]
			{
				//put statements here; 
			}
			write "Click on <<Step>> button to print the agent's data";
		}
		 
	}
   
}   
environment bounds: BOUNDS ;
entities {   
	species dummy skills: [SQLSKILL]
	 {  
		//Nothing
	 }   
	
	species locations {
		int id ;
		string name;
		rgb color <- rgb(rnd(255),rnd(255),0);
		geometry geom;
		reflex printdata{
			 write " id : " + id + "; name: " + name;
		}
		
	}
}      

experiment default_expr type: gui {
	output {
		
		display GlobalView {
			species locations ;
		}
	}
}


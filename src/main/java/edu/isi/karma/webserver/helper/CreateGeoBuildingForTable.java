package edu.isi.karma.webserver.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CreateGeoBuildingForTable {

	private Connection connection = null;
	private String osmFile_path;
	private Statement stmt = null;
	private ResultSet rs = null;

	public CreateGeoBuildingForTable(Connection connection, String osmFile_path) {
		this.connection = connection;
		this.osmFile_path = osmFile_path;
		try {
			this.stmt = this.connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void createOpenStreetMapBuildings() {
		CreateNodeDataForTable cnd = new CreateNodeDataForTable(
				this.connection, this.osmFile_path);
		cnd.createNodeDataforTable();

		try {
			rs = stmt.executeQuery("drop TABLE postgis.public.buildings_geo");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		try {
			rs = stmt
					.executeQuery("CREATE TABLE postgis.public.buildings_geo (Building_number integer PRIMARY KEY, Building_id character varying, Building_name character varying, State character varying, ele character varying, "
							+ "County_name character varying, UUID character varying, feature_id character varying, reviewed character varying,  Building_amenity character varying, source character varying,"
							+ " lat double precision, lon double precision, polygon_binary geography(polygon, 4326), polygon_text character varying,  Coordinate_System character varying,SRID integer)");

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		int ord = 1;
		String Coordinate_System = "WGS84";
		int srid = 4326;
		SAXReader saxReadering = new SAXReader();
		try {
			Document document = saxReadering.read(new File(this.osmFile_path));
			List listnode = document.selectNodes("//osm/node");
			Iterator iter_node = listnode.iterator();
			while (iter_node.hasNext()) {
				int colm_tag = 1;
				String NodeBuilding_name = " ";
				String NodeBuilding_state = " ";
				String NodeBuilding_ele = " ";
				String NodeBuilding_county_name = " ";
				String NodeBuilding_feature_id = " ";
				String NodeBuilding_import_uuid = " ";
				String NodeBuilding_reviewed = " ";
				String NodeBuilding_source = " ";
				String NodeBuilding_id = " ";
				String NodeBuilding_type = "no";
				String node_latlon = " ";
				String NodeBuilding_amenity = " ";
				String NodeLat = " ";
				String NodeLon = " ";
				Element ele_node = (Element) iter_node.next();

				for (Iterator ite = ele_node.attributeIterator(); ite.hasNext();) {
					Attribute attribute = (Attribute) ite.next();
					String name = attribute.getName();
					String value = attribute.getText();
					if (name.equals("id")) {// get node id, latitude, longitude;
						NodeBuilding_id = value;
					} else if (name.equals("lat")) {
						NodeLat = value;
					} else if (name.equals("lon")) {
						NodeLon = value;
					}
				}

				node_latlon = NodeLon + " " + NodeLat;
				List nodes = ele_node.elements("tag");
				for (Iterator its = nodes.iterator(); its.hasNext();) {
					Element elm_tag = (Element) its.next();
					for (Iterator iters = elm_tag.attributeIterator(); iters
							.hasNext();) {
						Attribute attributes = (Attribute) iters.next();
						String name = attributes.getName();
						String value = attributes.getText();
						if (value.equals("building")) {
							Attribute attribute_building = (Attribute) iters
									.next();
							NodeBuilding_type = attribute_building.getText();
						} else if (value.equals("name")) {
							Attribute attribute_name = (Attribute) iters.next();
							NodeBuilding_name = attribute_name.getText();
						} else if (value.equals("addr:state")) {
							Attribute attribute_state = (Attribute) iters
									.next();
							NodeBuilding_state = attribute_state.getText();
						} else if (value.equals("ele")) {
							Attribute attribute_ele = (Attribute) iters.next();
							NodeBuilding_ele = attribute_ele.getText();
						} else if (value.equals("gnis:county_name")) {
							Attribute attribute_county = (Attribute) iters
									.next();
							NodeBuilding_county_name = attribute_county
									.getText();
						} else if (value.equals("gnis:feature_id")) {
							Attribute attribute_feature = (Attribute) iters
									.next();
							NodeBuilding_feature_id = attribute_feature
									.getText();
						} else if (value.equals("gnis:import_uuid")) {
							Attribute attribute_uuid = (Attribute) iters.next();
							NodeBuilding_import_uuid = attribute_uuid.getText();
						} else if (value.equals("gnis:reviewed")) {
							Attribute attribute_reviewed = (Attribute) iters
									.next();
							NodeBuilding_reviewed = attribute_reviewed
									.getText();
						} else if (value.equals("source")) {
							Attribute attribute_source = (Attribute) iters
									.next();
							NodeBuilding_source = attribute_source.getText();
						} else if (value.equals("amenity")) {
							Attribute attribute_amenity = (Attribute) iters
									.next();
							NodeBuilding_amenity = attribute_amenity.getText();
						}
					}
				}

				if (NodeBuilding_type.equals("yes")
						|| NodeBuilding_amenity.equals("place_of_worship")) {
					try {
						rs = stmt
								.executeQuery("insert into postgis.public.buildings_geo(Building_number) values ("
										+ ord + ")");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						rs = stmt
								.executeQuery("update postgis.public.buildings_geo set Building_id=\'"
										+ NodeBuilding_id
										+ "\',"
										+ "Building_name=\'"
										+ NodeBuilding_name
										+ "\',State=\'"
										+ NodeBuilding_state
										+ "\',ele=\'"
										+ NodeBuilding_ele
										+ "\',"
										+ "County_name=\'"
										+ NodeBuilding_county_name
										+ "\',UUID=\'"
										+ NodeBuilding_import_uuid
										+ "\',feature_id=\'"
										+ NodeBuilding_feature_id
										+ "\', source=\'"
										+ NodeBuilding_source
										+ "\',"
										+ "reviewed=\'"
										+ NodeBuilding_reviewed
										+ "\',Building_amenity=\'"
										+ NodeBuilding_amenity
										+ "\',"
										+ "lat=\'"
										+ NodeLat
										+ "\',lon=\'"
										+ NodeLon
										+ "\', Coordinate_System=\'"
										+ Coordinate_System
										+ " \' , SRID=\'"
										+ srid
										+ " \' where Building_number="
										+ ord);
					} catch (SQLException ee) {
						ee.getStackTrace();
					}
					ord = ord + 1;
				}
			}

			List list = document.selectNodes("//osm/way");
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				String node_latlon = "";
				String NodeBuilding_name = " ";
				String NodeBuilding_state = " ";
				String NodeBuilding_ele = " ";
				String NodeBuilding_county_name = " ";
				String NodeBuilding_feature_id = " ";
				String NodeBuilding_import_uuid = " ";
				String NodeBuilding_reviewed = " ";
				String NodeBuilding_source = " ";
				String NodeBuilding_id = " ";
				String NodeBuilding_type = "no";
				String NodeBuilding_amenity = " ";
				Element ele = (Element) iter.next();

				for (Iterator ite = ele.attributeIterator(); ite.hasNext();) {// 处理提取出的way元素的属性值；
					Attribute attribute = (Attribute) ite.next();
					String name = attribute.getName();
					String value = attribute.getText();
					if (name.equals("id")) {
						NodeBuilding_id = value;
					}
				}

				List nods = ele.elements("nd");
				int colm_nd = 1;
				float lats = 0;
				float lons = 0;
				for (Iterator its = nods.iterator(); its.hasNext();) {
					Element elms = (Element) its.next();
					for (Iterator iters = elms.attributeIterator(); iters
							.hasNext();) {
						Attribute attribute = (Attribute) iters.next();
						String name = attribute.getName();
						String value = attribute.getText();
						if (name.equals("ref")) {
							try {
								rs = stmt
										.executeQuery("select lat,lon from postgis.public.nodestable where id=\'"
												+ value + "\';");
								while (rs.next()) {
									lats = rs.getFloat("lat");
									lons = rs.getFloat("lon");
									if (colm_nd == 1) {
										node_latlon = node_latlon + lons + " "
												+ lats;
									} else {
										node_latlon = node_latlon + "," + lons
												+ " " + lats;
									}
								}

							} catch (SQLException ee) {
								ee.getStackTrace();
							}
						}
					}
					colm_nd = colm_nd + 1;
				}

				List nodes = ele.elements("tag");
				for (Iterator its = nodes.iterator(); its.hasNext();) {
					Element elm_tag = (Element) its.next();
					for (Iterator iters = elm_tag.attributeIterator(); iters
							.hasNext();) {
						Attribute attributes = (Attribute) iters.next();
						String name = attributes.getName();
						String value = attributes.getText();
						if (value.equals("building")) {
							Attribute attribute_building = (Attribute) iters
									.next();
							NodeBuilding_type = attribute_building.getText();
						} else if (value.equals("name")) {
							Attribute attribute_name = (Attribute) iters.next();
							NodeBuilding_name = attribute_name.getText();
						} else if (value.equals("addr:state")) {
							Attribute attribute_state = (Attribute) iters
									.next();
							NodeBuilding_state = attribute_state.getText();
						} else if (value.equals("ele")) {
							Attribute attribute_ele = (Attribute) iters.next();
							NodeBuilding_ele = attribute_ele.getText();
						} else if (value.equals("gnis:county_name")) {
							Attribute attribute_county = (Attribute) iters
									.next();
							NodeBuilding_county_name = attribute_county
									.getText();
						} else if (value.equals("gnis:feature_id")) {
							Attribute attribute_feature = (Attribute) iters
									.next();
							NodeBuilding_feature_id = attribute_feature
									.getText();
						} else if (value.equals("gnis:import_uuid")) {
							Attribute attribute_uuid = (Attribute) iters.next();
							NodeBuilding_import_uuid = attribute_uuid.getText();
						} else if (value.equals("gnis:reviewed")) {
							Attribute attribute_reviewed = (Attribute) iters
									.next();
							NodeBuilding_reviewed = attribute_reviewed
									.getText();
						} else if (value.equals("source")) {
							Attribute attribute_source = (Attribute) iters
									.next();
							NodeBuilding_source = attribute_source.getText();
						} else if (value.equals("amenity")) {
							Attribute attribute_amenity = (Attribute) iters
									.next();
							NodeBuilding_amenity = attribute_amenity.getText();
						}
					}

				}

				if (NodeBuilding_type.equals("yes")) {
					try {
						rs = stmt
								.executeQuery("insert into postgis.public.buildings_geo(Building_number) values ("
										+ ord + ")");
					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						System.out.println("Building Name is:"+NodeBuilding_name);
						rs = stmt
								.executeQuery("update postgis.public.buildings_geo set Building_id=\'"
										+ NodeBuilding_id
										+ "\',"
										+ "Building_name=\'"
										+ NodeBuilding_name
										+ "\',State=\'"
										+ NodeBuilding_state
										+ "\',ele=\'"
										+ NodeBuilding_ele
										+ "\',"
										+ "County_name=\'"
										+ NodeBuilding_county_name
										+ "\',UUID=\'"
										+ NodeBuilding_import_uuid
										+ "\',feature_id=\'"
										+ NodeBuilding_feature_id
										+ "\', source=\'"
										+ NodeBuilding_source
										+ "\',"
										+ "reviewed=\'"
										+ NodeBuilding_reviewed
										+ "\',Building_amenity=\'"
										+ NodeBuilding_amenity
										+ "\' , Coordinate_System=\'"
										+ Coordinate_System
										+ " \', SRID=\'"
										+ srid
										+ " \'  where Building_number="
										+ ord);

					} catch (SQLException ee) {
						ee.getStackTrace();
					}

					try {
						rs = stmt
								.executeQuery("update postgis.public.buildings_geo set polygon_binary=ST_GeographyFromText(\'SRID=4326; polygon(("
										+ node_latlon
										+ "))\') , polygon_text=\'"
										+ node_latlon
										+ " \'   where Building_number=" + ord);
					} catch (SQLException ee) {
						ee.getStackTrace();
					}
					ord = ord + 1;
				}

			}

			try {
				rs = stmt
						.executeQuery("	Copy (Select * From postgis.public.buildings_geo) To '/tmp/buildings_geo.csv' CSV HEADER;");
			} catch (SQLException ee) {
				ee.getStackTrace();
			}

		} catch (DocumentException e) {
			e.getStackTrace();
		}

	}

}

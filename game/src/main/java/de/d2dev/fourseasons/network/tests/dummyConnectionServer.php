<?php
	/*
		This php file is used to emulate an http connection server for client
		side testing purposes.
	*/
	function failed_response() {
		echo "<?xml version=\"1.0\"?><connectionServerResponse><failed/></connectionServerResponse>";
	}
	
	function succeeded_response() {
		echo "<?xml version=\"1.0\"?><connectionServerResponse><succeeded/></connectionServerResponse>";
	}
	
	function getServers_response() {
		echo "<?xml version=\"1.0\"?>
				<connectionServerResponse>
					<servers>
						<server>
							<ip>127.168.2.1</ip>
							<port>43891</port> 
							<description>
							</description>
						</server>
						<server>
							<ip>126.077.088.23</ip>
							<port>8000</port>
							<description> 
								<game>Mid-War</game>
								<type>ZONE-SERVER</type>
								<zone x=\"100\" y=\"100\" z=\"100\" />
								<ID>5</ID>
							</description>
						</server>
					</servers>
				</connectionServerResponse>";
	}
	
	$request= simplexml_load_string( file_get_contents('php://input') );
	
	if ($request->getName() == "connectionServerRequest") {
		$type = $request->children();
		
		if ($type->getName() == "getServers") {
			getServers_response();
			return;
		}
		
		if ($type->getName() == "addServer") {
			succeeded_response();
			return;
		}
		
		if ($type->getName() == "removeServer") {
			succeeded_response();
			return;
		}
	}
	
	failed_response();
?>

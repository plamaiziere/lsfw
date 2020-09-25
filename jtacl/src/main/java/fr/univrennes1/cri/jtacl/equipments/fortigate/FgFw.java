package fr.univrennes1.cri.jtacl.equipments.fortigate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRef;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefMap;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.equipments.checkpointR80.*;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.*;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class FgFw extends GenericEquipment {

	protected class FgIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;

		public FgIface(Iface iface) {
			_iface = iface;
		}

		public Iface getIface() {
			return _iface;
		}

		public void setIface(Iface iface) {
			_iface = iface;
		}

		public String getDescription() {
			return _description;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}
	}

	/**
	 * interfaces
	 */
	protected HashMap<String, FgIface> _fnFwIfaces
		= new HashMap<>();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/**
	 * IP cross references map
	 */
	protected IPCrossRefMap _netCrossRef = new IPCrossRefMap();

	/**
	 * Services cross references map
	 */
	protected ServiceCrossRefMap _serviceCrossRef = new ServiceCrossRefMap();

	/**
	 * IP cross references map
	 */
	IPCrossRefMap getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * service cross references map
	 */
	ServiceCrossRefMap getServiceCrossRef() {
		return _serviceCrossRef;
	}

    /*
	 * services keyed by origin key
	 */
	protected HashMap<String, FgService> _fgServices
			= new HashMap<>();


	/*
	 * network objects keyed origin key
	 */
	protected HashMap<String, FgNetworkObject> _fgNetworks
			= new HashMap<>();

    public HashMap<String, FgService> getFgServices() {
        return _fgServices;
    }

    public HashMap<String, FgNetworkObject> getFgNetworks() {
        return _fgNetworks;
    }

	/**
	 * Sorted list of services orgin key
	 * @return a sorted list of services origin key.
	 */
	public List<String> getServicesName() {

		List<String> list = new LinkedList<>();
		list.addAll(_fgServices.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * Sorted list of networks origin key
	 * @return a sorted list of networks origin key
	 */
	public List<String> getNetworksName() {

		List<String> list = new LinkedList<>();
		list.addAll(_fgNetworks.keySet());
		Collections.sort(list);
		return list;
	}

    FgFwShell _shell = new FgFwShell(this);

	/**
	 * Create a new {@link FgFw} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public FgFw(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
	}

	protected void loadIfaces(Document doc) {

		NodeList list = doc.getElementsByTagName("iface");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String ifIp = e.getAttribute("ip");
			String ifNetwork = e.getAttribute("network");

			String s = "name: " + name + " comment: " + comment +
					" IP: " + ifIp + " network: " + ifNetwork;

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing interface name: " + s);

			if (ifIp.isEmpty())
				throw new JtaclConfigurationException("Missing interface IP: " + s);

			IPNet ip;
			try {
				ip = new IPNet(ifIp);
				ip = ip.hostAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface IP: " + s);
			}

			IPNet network;
			try {
				/*
				 * If network attribute is empty, use the network address of the IP
				 * instead.
				 */
				if (ifNetwork.isEmpty())
					network = new IPNet(ifIp);
				else
					network = new IPNet(ifNetwork);
				network = network.networkAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface network: " + s);
			}

			/*
			 * Append the link to an existing Iface or create a new one.
			 */
			FgIface fnfwIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
				fnfwIface = new FgIface(iface);
				_fnFwIfaces.put(name, fnfwIface);
			}
			iface.addLink(ip, network);
		}
	}

	protected void throwCfgException(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		throw new JtaclConfigurationException(s);
	}

	protected void warnConfig(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		Log.config().warning(s);
	}

	@Override
	public void configure() {
		if (_configurationFileName.isEmpty())
			return;

		/*
		 * Read the XML configuration file
		 */
		famAdd(_configurationFileName);
		Document doc = XMLUtils.getXMLDocument(_configurationFileName);
		loadOptionsFromXML(doc);
		loadFiltersFromXML(doc);

		loadIfaces(doc);
/*

		loadConfiguration(doc);

		linkServices();
		linkNetworkObjects();
*/

		/*
		 * routing
		 */
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		/*
		 * compute cross reference
		 */
/*
		if (_monitorOptions.getXref())
			CrossReferences();
*/
	}

	protected void loadConfiguration(Document doc) {

         /* fwpolicy */
		NodeList list = doc.getElementsByTagName("fwpolicy");
		if (list.getLength() < 1) {
			throwCfgException("At least one fwpolicy must be specified", false);
		}

		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing policy file name", false);
        }

        for (String f: filenames) {
            famAdd(f);
            FileReader jf;
            JsonNode rootNode = null;
            try {
                jf = new FileReader(f);
                ObjectMapper objectMapper = new ObjectMapper();
                rootNode = objectMapper.readTree(jf);
            } catch (IOException ex) {
                throwCfgException("Cannot read file " + ex.getMessage(), false);
            }
            JsonNode dictNode = rootNode.path("services");
            loadJsonServices(dictNode);
            //JsonNode layersNode = rootNode.path("layers");
            //loadJsonCpLayers(layersNode);
        }

        // implicit drop rule at the end of the root layer
        //CpFwRule fwdrop = CpFwRule.newImplicitDropRule(_rootLayer);
        //_rootLayer.getRules().add(fwdrop);

    }

    protected int parsePortNumber(String port) {
	    int i = 0;
        try {
            i = Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            throwCfgException("invalid port number: " + port, true);
        }
        return i;
    }

    protected List<PortSpec> parsePortsRanges(String portsRanges) {
	    List<PortSpec> portsSpecs = new ArrayList<>();
	    String[] lpr = portsRanges.split(":");
	    if (lpr.length > 2) throwCfgException("invalid source/destination ports ranges: " + portsRanges, true);
	    if (lpr.length == 1) {
	        portsSpecs.add(null);
	        portsSpecs.add(parsePortRange(lpr[0]));
        } else {
   	        portsSpecs.add(parsePortRange(lpr[0]));
	        portsSpecs.add(parsePortRange(lpr[1]));
        }
	        return portsSpecs;
	}

    protected PortSpec parsePortRange(String portrange) {
	    PortSpec portSpec = new PortSpec();

	    //XXX: form : port1 port2 range1-range1 port3 range2-range2 ...
	    String[] slp = portrange.split(" ");
        for (String sp: slp) {
            String[] sr = sp.split("-");
            if (sr.length > 2) throwCfgException("invalid port range: " + portrange, true);

            int port1 = parsePortNumber(sr[0]);
            int port2 = port1;
            if (sr.length == 2) {
                port2 = parsePortNumber(sr[1]);
            }
            portSpec.add(new PortRange(port1, port2));
        }
        return portSpec;
	}

	List<IPRangeable> parseFqdn(String fqdn) {
	    List<IPRangeable> ips = new LinkedList<>();
        try {
            List<IPNet> lip = IPNet.getAllByName(fqdn, IPversion.IPV4);
            for (IPNet ip: lip) {
                ips.add(new IPRange(ip));
            }
        } catch (UnknownHostException e) {
            throwCfgException("cannot resolve FQDN: " + fqdn, true);
        }
        return ips;
	}

	List<IPRangeable> parseIpRange(String iprange) {
	    List<IPRangeable> ips = new LinkedList<>();

	    IPNet ip;
	    ip = new IPNet(iprange);
	    ips.add(new IPRange(ip, ip);
        try {
            List<IPNet> lip = IPNet.getAllByName(fqdn, IPversion.IPV4);
            for (IPNet ip: lip) {
                ips.add(new IPRange(ip));
            }
        } catch (UnknownHostException e) {
            throwCfgException("cannot resolve FQDN: " + fqdn, true);
        }
        return ips;
	}

	/*
	 * parse and load services objects from JSON
	 */
	protected void loadJsonServices(JsonNode objectsDictionary) {

	    Iterator<JsonNode> it = objectsDictionary.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
            String sprotocol = n.path("protocol").textValue();
            String siprange = n.path("iprange").textValue();
            String sfqdn = n.path("fqdn").textValue();
	        String scomment = n.path("comments").textValue();

	        parseFqdn()
	        FgService service = null;
	        if (protocol.equals("TCP/UDP/SCTP")) {

            }
	        CpNetworkObject network = null;
	        CpFwRuleBaseAction action = null;
	        CpObject obj = null;
	        CpAny any = null;
	        CpLayer layer = null;
	        switch (className) {
            }
            }


	protected IPCrossRef getIPNetCrossRef(IPRangeable iprange) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		IPCrossRef ref = _netCrossRef.get(iprange);
		if (ref == null) {
			ref = new IPCrossRef(iprange);
			_netCrossRef.put(ref);
		}
		return ref;
	}

	protected ServiceCrossRef getServiceCrossRef(PortRange portrange) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		ServiceCrossRef ref = _serviceCrossRef.get(portrange);
		if (ref == null) {
			ref = new ServiceCrossRef(portrange);
			_serviceCrossRef.put(ref);
		}
		return ref;
	}

}

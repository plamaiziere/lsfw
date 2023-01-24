# lsfw
lsfw: a tool to list firewall rules in network equipments.

The goal of lsfw is to help network administrators to deal with fire-walling on a huge network wih many firewall rules.

Lsfw uses the configuration of the network equipments and builds a (light) model of the network described by these equipments.
This allows to probe for access-list matching all over the network, doing routing, fire-walling or packet transformation.

# Network equipments
Lsfw implements:

* Cisco routers
*  Cisco firewall (pix, fwsm)
*  OpenBSD Packet Filter
*  R70 ⇐ Check Point Gaia < R80
*  Check Point Gaia >= R80
*  Fortinet Fortigate 6.X
*  Proxmox VE Firewall 7.0

Full documentation at https://github.com/plamaiziere/lsfw/wiki

Lsfw is developed by and used at Université de Rennes 1 / France, see COPYING for licenses

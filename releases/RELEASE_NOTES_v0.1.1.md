# Android POC v0.1.1

Status: REVIEW / POC

This release demonstrates the four-screen navigation model: NOW, HISTORY, TARGET, and ROUTE. It uses Android GNSS and orientation sensors for local measured state, preserves navigation-state history, permits a historical state to become a target, and renders a visible return-route vector.

The LOCAL_SYSTEM, INTER_SYSTEM_LOCAL_GROUP, TRANSAXIAL, and MILKY_WAY scenes are demonstration geometry only. They are not authoritative ephemerides or flight-navigation data.

Real-device evidence from the initial test confirms that the NOW screen, GNSS/local position, reported fix uncertainty, orientation readout, and 3D rendering operate on-device. HISTORY, TARGET, ROUTE, return-to-history navigation, and large-scale transforms require further device validation.

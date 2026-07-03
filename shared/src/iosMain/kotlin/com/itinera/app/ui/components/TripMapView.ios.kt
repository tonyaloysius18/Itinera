package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.UIKit.UIColor
import platform.UIKit.systemBlueColor
import platform.darwin.NSObject

/**
 * Apple Maps trip view: a pin per city, a line per leg, region framed to fit all
 * points. The delegate is held in `remember` — MKMapView keeps it weakly, so
 * without a strong reference the polyline renderer callbacks would silently stop.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun TripMapView(
    markers: List<MapMarker>,
    routes: List<MapRoute>,
    modifier: Modifier,
) {
    val delegate = remember { TripMapDelegate() }

    UIKitView(
        factory = {
            val map = MKMapView()
            map.delegate = delegate

            // pins
            markers.forEach { m ->
                val ann = MKPointAnnotation()
                ann.setCoordinate(CLLocationCoordinate2DMake(m.lat, m.lng))
                ann.setTitle(m.label)
                map.addAnnotation(ann)
            }

            // leg lines
            routes.forEach { r ->
                memScoped {
                    val coords = allocArray<CLLocationCoordinate2D>(2)
                    coords[0].latitude = r.fromLat; coords[0].longitude = r.fromLng
                    coords[1].latitude = r.toLat;   coords[1].longitude = r.toLng
                    map.addOverlay(MKPolyline.polylineWithCoordinates(coords, 2u))
                }
            }

            // frame all points with padding
            if (markers.isNotEmpty()) {
                val lats = markers.map { it.lat }
                val lngs = markers.map { it.lng }
                val cLat = (lats.min() + lats.max()) / 2.0
                val cLng = (lngs.min() + lngs.max()) / 2.0
                val spanLat = ((lats.max() - lats.min()) * 1.5).coerceAtLeast(0.5)
                val spanLng = ((lngs.max() - lngs.min()) * 1.5).coerceAtLeast(0.5)
                map.setRegion(
                    MKCoordinateRegionMake(
                        CLLocationCoordinate2DMake(cLat, cLng),
                        MKCoordinateSpanMake(spanLat, spanLng),
                    ),
                    animated = false,
                )
            }
            map
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalForeignApi::class)
private class TripMapDelegate : NSObject(), MKMapViewDelegateProtocol {
    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        val polyline = rendererForOverlay as? MKPolyline
            ?: return MKOverlayRenderer(overlay = rendererForOverlay)
        return MKPolylineRenderer(polyline = polyline).apply {
            strokeColor = UIColor.systemBlueColor
            lineWidth = 3.0
        }
    }
}
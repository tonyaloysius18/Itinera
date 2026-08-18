import CoreLocation
import Foundation
import MapLibre
import Shared
import UIKit

private let openFreeMapStyle = URL(string: "https://tiles.openfreemap.org/styles/liberty")!

private struct TripMapPayload: Decodable {
    let markers: [TripMapMarker]
    let routes: [TripMapRoute]
}

private struct TripMapMarker: Decodable {
    let lat: Double
    let lng: Double
    let label: String
}

private struct TripMapRoute: Decodable {
    let fromLat: Double
    let fromLng: Double
    let toLat: Double
    let toLng: Double
}

enum MapLibreTripMapFactory {
    static func makeMap(payloadJSON: String) -> UIView {
        guard
            let data = payloadJSON.data(using: .utf8),
            let payload = try? JSONDecoder().decode(TripMapPayload.self, from: data)
        else {
            return messageView("We couldn't load this trip map.")
        }
        return MapLibreTripMapView(payload: payload)
    }

    private static func messageView(_ message: String) -> UIView {
        let label = UILabel()
        label.text = message
        label.textAlignment = .center
        label.textColor = .secondaryLabel
        label.numberOfLines = 0
        return label
    }
}

private final class MapLibreTripMapView: UIView, MLNMapViewDelegate {
    private let mapView: MLNMapView
    private let payload: TripMapPayload

    init(payload: TripMapPayload) {
        self.payload = payload
        self.mapView = MLNMapView(frame: .zero, styleURL: openFreeMapStyle)
        super.init(frame: .zero)

        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        mapView.delegate = self
        mapView.compassView.isHidden = false
        addSubview(mapView)

        payload.markers.forEach { item in
            let marker = MLNPointAnnotation()
            marker.coordinate = CLLocationCoordinate2D(latitude: item.lat, longitude: item.lng)
            marker.title = item.label
            mapView.addAnnotation(marker)
        }

        payload.routes.forEach { item in
            var points = [
                CLLocationCoordinate2D(latitude: item.fromLat, longitude: item.fromLng),
                CLLocationCoordinate2D(latitude: item.toLat, longitude: item.toLng),
            ]
            mapView.addAnnotation(MLNPolyline(coordinates: &points, count: UInt(points.count)))
        }

        frameTrip()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        mapView.frame = bounds
    }

    func mapView(
        _ mapView: MLNMapView,
        strokeColorForShapeAnnotation annotation: MLNShape
    ) -> UIColor {
        UIColor(red: 45 / 255, green: 127 / 255, blue: 249 / 255, alpha: 1)
    }

    func mapView(
        _ mapView: MLNMapView,
        lineWidthForPolylineAnnotation annotation: MLNPolyline
    ) -> CGFloat {
        4
    }

    private func frameTrip() {
        guard let first = payload.markers.first else { return }
        if payload.markers.count == 1 {
            mapView.setCenter(
                CLLocationCoordinate2D(latitude: first.lat, longitude: first.lng),
                zoomLevel: 9,
                animated: false
            )
            return
        }

        let coordinates = payload.markers.map {
            CLLocationCoordinate2D(latitude: $0.lat, longitude: $0.lng)
        }
        let bounds = coordinates.dropFirst().reduce(
            MLNCoordinateBounds(sw: coordinates[0], ne: coordinates[0])
        ) { current, coordinate in
            MLNCoordinateBounds(
                sw: CLLocationCoordinate2D(
                    latitude: min(current.sw.latitude, coordinate.latitude),
                    longitude: min(current.sw.longitude, coordinate.longitude)
                ),
                ne: CLLocationCoordinate2D(
                    latitude: max(current.ne.latitude, coordinate.latitude),
                    longitude: max(current.ne.longitude, coordinate.longitude)
                )
            )
        }
        mapView.setVisibleCoordinateBounds(
            bounds,
            edgePadding: UIEdgeInsets(top: 56, left: 48, bottom: 56, right: 48),
            animated: false
        )
    }
}

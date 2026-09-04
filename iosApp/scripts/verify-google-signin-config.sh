#!/bin/sh
#
# Fails the build when GIDClientID and the declared reversed-client-ID URL
# scheme point at different Google OAuth clients.
#
# The GoogleSignIn SDK derives the URL scheme it requires from GIDClientID and,
# if the app does not declare that exact scheme, raises an uncaught
# NSInvalidArgumentException from inside signIn(withPresenting:) — an
# Objective-C exception no Swift or Kotlin catch can intercept. The app hard
# crashes the moment the user taps "Sign in with Google".
#
# Nothing else in the build catches this: both keys are individually valid, so
# the mismatch only shows up at runtime, on a device, in the hands of a tester.
# TestFlight build 1.0 (4) shipped exactly this way.

set -eu

# Validate the SOURCE Info.plist, not the built one. Xcode runs
# ProcessInfoPlistFile after every shell script phase, so ${TARGET_BUILD_DIR}
# still holds the PREVIOUS build's plist while this runs — checking it would
# silently validate stale output and pass a broken config.
PLIST="${SRCROOT}/${INFOPLIST_FILE:-}"

if [ -z "${INFOPLIST_FILE:-}" ] || [ ! -f "$PLIST" ]; then
    echo "warning: Google sign-in config check skipped — no Info.plist at ${PLIST}"
    exit 0
fi

read_key() {
    /usr/libexec/PlistBuddy -c "Print :$1" "$PLIST" 2>/dev/null || true
}

CLIENT_ID=$(read_key "GIDClientID")

# No Google sign-in configured at all — nothing to validate.
if [ -z "$CLIENT_ID" ]; then
    exit 0
fi

# The SDK strips the ".apps.googleusercontent.com" suffix and prefixes
# "com.googleusercontent.apps." to build the scheme it looks for.
EXPECTED_SCHEME="com.googleusercontent.apps.${CLIENT_ID%.apps.googleusercontent.com}"

# Walk every declared URL scheme; any one of them matching is enough.
found=0
url_type=0
while :; do
    if ! /usr/libexec/PlistBuddy -c "Print :CFBundleURLTypes:${url_type}" "$PLIST" >/dev/null 2>&1; then
        break
    fi
    scheme=0
    while :; do
        value=$(/usr/libexec/PlistBuddy \
            -c "Print :CFBundleURLTypes:${url_type}:CFBundleURLSchemes:${scheme}" \
            "$PLIST" 2>/dev/null || true)
        [ -z "$value" ] && break
        if [ "$value" = "$EXPECTED_SCHEME" ]; then
            found=1
            break
        fi
        scheme=$((scheme + 1))
    done
    [ "$found" -eq 1 ] && break
    url_type=$((url_type + 1))
done

if [ "$found" -eq 1 ]; then
    exit 0
fi

echo "error: Google sign-in would crash at runtime — GIDClientID and CFBundleURLSchemes disagree."
echo "error:   GIDClientID      $CLIENT_ID"
echo "error:   requires scheme  $EXPECTED_SCHEME"
echo "error: Add that scheme to CFBundleURLTypes in Info.plist, or correct GIDClientID so the two"
echo "error: describe the same OAuth client. Tapping the Google button raises an uncaught"
echo "error: NSInvalidArgumentException otherwise (GIDSignIn.m, 'missing support for the following URL schemes')."
exit 1

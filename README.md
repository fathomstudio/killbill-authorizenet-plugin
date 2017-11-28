# Kill Bill BluePay Plugin

Kill Bill plugin for BluePay.

## Building
`mvn clean install`

## Installing
Copy the JAR (`target/killbill-authorizenet-plugin-<version>.jar`) to the Kill Bill path `/var/lib/killbill/bundles/plugins/java/killbill-authorizenet-plugin/<version>/killbill-authorizenet-plugin-<version>.jar`. This path can change with the `org.killbill.osgi.bundle.install.dir` property.
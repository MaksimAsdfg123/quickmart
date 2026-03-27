rootProject.name = "quickmart"

include(":backend")
project(":backend").projectDir = file("app/backend")

include(":ui-tests")
project(":ui-tests").projectDir = file("tests/frontend")

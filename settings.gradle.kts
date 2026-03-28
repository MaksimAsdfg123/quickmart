rootProject.name = "quickmart"

include(":backend")
project(":backend").projectDir = file("app/backend")

include(":api-tests")
project(":api-tests").projectDir = file("tests/backend")

include(":ui-tests")
project(":ui-tests").projectDir = file("tests/frontend")

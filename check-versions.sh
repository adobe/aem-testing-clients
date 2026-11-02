mvn --no-transfer-progress \
  versions:display-dependency-updates \
  versions:display-plugin-updates \
  -DprocessDependencyManagementTransitive=false

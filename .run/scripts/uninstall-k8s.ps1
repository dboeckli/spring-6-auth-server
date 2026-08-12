cd target/helm/repo

helm uninstall spring-6-auth-server-chart --namespace spring-6-auth-server

kubectl delete pod -n spring-6-auth-server --field-selector=status.phase==Succeeded
kubectl delete pod -n spring-6-auth-server --field-selector=status.phase==Failed
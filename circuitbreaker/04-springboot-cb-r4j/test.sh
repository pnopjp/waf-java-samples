curl http://localhost:8080/test1/500 
curl http://localhost:8080/test1/500 
curl http://localhost:8080/test1/500

curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 
curl http://localhost:8080/test1/200 

curl http://localhost:8080/test1/200  # closed -> open

echo "waiting...."
sleep 10

curl http://localhost:8080/test1/500
curl http://localhost:8080/test1/500
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/500
curl http://localhost:8080/test1/500

echo "waiting...."
sleep 10

curl http://localhost:8080/test1/500
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200
curl http://localhost:8080/test1/200




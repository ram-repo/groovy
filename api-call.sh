curl --request GET \
  'https://your-domain-endpoint/_cat/health' \
  --aws-sigv4 aws:amz:us-east-1:es \
  --user "${AWS_ACCESS_KEY_ID}:${AWS_SECRET_ACCESS_KEY}" \
  --header "x-amz-security-token: ${AWS_SESSION_TOKEN}" \
  --header 'Accept: application/json'

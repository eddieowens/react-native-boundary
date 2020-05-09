require 'json'

package = JSON.parse(File.read(File.join(__dir__, '../package.json')))

Pod::Spec.new do |s|
  s.name         = "RNBoundary"
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = "https://github.com/woffu/react-native-boundary#readme"
  s.platform     = :ios, "9.0"

  s.source       = { :git => "https://github.com/woffu/react-native-boundary.git", :tag => "#{s.version}" }
  s.source_files  = "*.{h,m}"
  s.requires_arc = true

  s.dependency 'React'
end

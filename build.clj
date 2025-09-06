(ns build
  "Build script for physiotherapy scheduler"
  (:require [clojure.tools.build.api :as b]))

(def lib 'physiotherapy-scheduler/physiotherapy-scheduler)
(def version "1.0.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s.jar" (name lib)))

(defn clean
  "Delete the build target directory"
  [_]
  (b/delete {:path "target"}))

(defn uber
  "Build the uberjar"
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'app.core}))

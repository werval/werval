module.exports = function(grunt) {
    "use strict";

    // Project configuration.
    grunt.initConfig({
        // Metadata.
        pkg: grunt.file.readJSON('package.json'),
        banner: '/**\n' +
                ' * Beer Database v<%= pkg.version %>\n' +
                ' * Copyright (c) <%= grunt.template.today("yyyy") %> the original author or authors\n' +
                ' * Licensed under <%= _.pluck(pkg.licenses, "url").join(", ") %>\n' +
                ' */\n',
        // Tasks configuration.
        clean: {
            build: ['build/grunt']
        },
        jshint: {
            options: {
                jshintrc: 'src/main/assets/js/.jshintrc'
            },
            gruntfile: {
                src: 'Gruntfile.js'
            },
            src: {
                src: ['src/main/assets/js/*.js']
            }
        },
        concat: {
            options: {
                banner: '<%= banner %>',
                stripBanners: false
            },
            js: {
                src: [
                    'bower_components/jquery/jquery.js',
                    // 'bower_components/bootstrap/js/transition.js',
                    // 'bower_components/bootstrap/js/alert.js',
                    // 'bower_components/bootstrap/js/button.js',
                    // 'bower_components/bootstrap/js/carousel.js',
                    // 'bower_components/bootstrap/js/collapse.js',
                    // 'bower_components/bootstrap/js/dropdown.js',
                    // 'bower_components/bootstrap/js/modal.js',
                    // 'bower_components/bootstrap/js/tooltip.js',
                    // 'bower_components/bootstrap/js/popover.js',
                    // 'bower_components/bootstrap/js/scrollspy.js',
                    // 'bower_components/bootstrap/js/tab.js',
                    // 'bower_components/bootstrap/js/affix.js',
                    'src/main/assets/js/main.js'
                ],
                dest: 'build/grunt/assets/js/main.js'
            }
        },
        uglify: {
            options: {
                banner: '<%= banner %>',
                report: 'min'
            },
            js: {
                src: ['<%= concat.js.dest %>'],
                dest: 'build/grunt/assets/js/main.min.js'
            }
        },
        copy: {
            less: {
                expand: true,
                flatten: true,
                src: [
                    "bower_components/bootstrap/less/*",
                    "src/main/assets/less/*"
                ],
                dest: 'build/grunt/css-tmp'
            },
            fonts: {
                expand: true,
                flatten: true,
                src: ["bower_components/bootstrap/fonts/*"],
                dest: 'build/grunt/assets/fonts'
            }
        },
        recess: {
            options: {
                compile: true,
                banner: '<%= banner %>'
            },
            main: {
                src: ['build/grunt/css-tmp/main.less'],
                dest: 'build/grunt/assets/css/main.css'
            },
            min: {
                options: {
                    compress: true
                },
                src: ['build/grunt/css-tmp/main.less'],
                dest: 'build/grunt/assets/css/main.min.css'
            }
        }
    });


    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-recess');

    // Check task.
    grunt.registerTask('check', ['jshint']);

    // JS distribution task.
    grunt.registerTask('assets-js', ['concat:js', 'uglify:js']);

    // CSS distribution task.
    grunt.registerTask('assets-css', ['copy', 'recess']);

    // Full distribution task.
    grunt.registerTask('assets', ['assets-css', 'assets-js']);

    // Default task.
    grunt.registerTask('default', ['clean', 'assets']);

};
